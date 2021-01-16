package pipeline.orchestrator.protobuf;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BuildDependencyGraphTest {

    private static final String DESCRIPTOR_NAME_1 = "Descriptor 1";
    private static final String DESCRIPTOR_NAME_2 = "Descriptor 2";
    private static final String DESCRIPTOR_NAME_3 = "Descriptor 3";
    private static final String CIRCULAR_NAME_1 = "Circular 1";
    private static final String CIRCULAR_NAME_2 = "Circular 2";
    private static final String SELF_CYCLE_NAME = "Self Cycle";

    // No dependencies
    private static final FileDescriptorProto DESCRIPTOR_PROTO_1 = FileDescriptorProto.newBuilder()
            .setName(DESCRIPTOR_NAME_1)
            .build();

    // Dependencies: 1
    private static final FileDescriptorProto DESCRIPTOR_PROTO_2 = FileDescriptorProto.newBuilder()
            .setName(DESCRIPTOR_NAME_2)
            .addDependency(DESCRIPTOR_NAME_1)
            .build();

    // Dependencies: 1, 2
    private static final FileDescriptorProto DESCRIPTOR_PROTO_3 = FileDescriptorProto.newBuilder()
            .setName(DESCRIPTOR_NAME_3)
            .addDependency(DESCRIPTOR_NAME_1)
            .addDependency(DESCRIPTOR_NAME_2)
            .build();

    private static final FileDescriptorProto CIRCULAR_PROTO_1 = FileDescriptorProto.newBuilder()
            .setName(CIRCULAR_NAME_1)
            .addDependency(CIRCULAR_NAME_2)
            .build();

    private static final FileDescriptorProto CIRCULAR_PROTO_2 = FileDescriptorProto.newBuilder()
            .setName(CIRCULAR_NAME_2)
            .addDependency(CIRCULAR_NAME_1)
            .build();

    private static final FileDescriptorProto SELF_CYCLE_PROTO = FileDescriptorProto.newBuilder()
            .setName(SELF_CYCLE_NAME)
            .addDependency(SELF_CYCLE_NAME)
            .build();

    @Test
    public void testSingleFile() {
        // Given
        FileDescriptorSet descriptorSet = FileDescriptorSet.newBuilder()
                .addFile(DESCRIPTOR_PROTO_1)
                .build();

        // When
        ImmutableGraph<FileDescriptorProto> dependenciesGraph = DescriptorProtos.buildDependencyGraph(descriptorSet);

        // Then
        List<FileDescriptorProto> nodes = new ArrayList<>(dependenciesGraph.nodes());
        assertEquals(1, nodes.size());
        assertEquals(DESCRIPTOR_PROTO_1, nodes.get(0));

        assertEquals(0, dependenciesGraph.edges().size());
    }

    @Test
    public void testTwoFilesSingleDependency() {
        // Given
        FileDescriptorSet descriptorSet = FileDescriptorSet.newBuilder()
                .addFile(DESCRIPTOR_PROTO_1)
                .addFile(DESCRIPTOR_PROTO_2)
                .build();

        // When
        ImmutableGraph<FileDescriptorProto> dependenciesGraph = DescriptorProtos.buildDependencyGraph(descriptorSet);

        // Then
        ImmutableGraph<FileDescriptorProto> expected = GraphBuilder.directed()
                .<FileDescriptorProto>immutable()
                .putEdge(EndpointPair.ordered(DESCRIPTOR_PROTO_2, DESCRIPTOR_PROTO_1))
                .build();

        assertEquals(expected, dependenciesGraph);
    }

    @Test
    public void testThreeFilesThreeDependencies() {
        // Given
        FileDescriptorSet descriptorSet = FileDescriptorSet.newBuilder()
                .addFile(DESCRIPTOR_PROTO_3)
                .addFile(DESCRIPTOR_PROTO_2)
                .addFile(DESCRIPTOR_PROTO_1)
                .build();

        // When
        ImmutableGraph<FileDescriptorProto> dependenciesGraph = DescriptorProtos.buildDependencyGraph(descriptorSet);

        // Then
        ImmutableGraph<FileDescriptorProto> expected = GraphBuilder.directed()
                .<FileDescriptorProto>immutable()
                .putEdge(EndpointPair.ordered(DESCRIPTOR_PROTO_3, DESCRIPTOR_PROTO_2))
                .putEdge(EndpointPair.ordered(DESCRIPTOR_PROTO_3, DESCRIPTOR_PROTO_1))
                .putEdge(EndpointPair.ordered(DESCRIPTOR_PROTO_2, DESCRIPTOR_PROTO_1))
                .build();

        assertEquals(expected, dependenciesGraph);
    }

    @Test
    public void testMissingDependency() {
        // Given
        FileDescriptorSet descriptorSet = FileDescriptorSet.newBuilder()
                .addFile(DESCRIPTOR_PROTO_2)
                .build();

        // When
        assertThrows(MissingDependencyException.class, () -> DescriptorProtos.buildDependencyGraph(descriptorSet));
    }

    @Test
    public void testCircularDependencies() {
        // Given
        FileDescriptorSet descriptorSet = FileDescriptorSet.newBuilder()
                .addFile(CIRCULAR_PROTO_1)
                .addFile(CIRCULAR_PROTO_2)
                .build();

        // When
        assertThrows(CircularDependenciesException.class, () -> DescriptorProtos.buildDependencyGraph(descriptorSet));
    }

    @Test
    public void testSelfCycle() {
        // Given
        FileDescriptorSet descriptorSet = FileDescriptorSet.newBuilder()
                .addFile(SELF_CYCLE_PROTO)
                .build();

        // When
        assertThrows(IllegalArgumentException.class, () -> DescriptorProtos.buildDependencyGraph(descriptorSet));
    }
}
