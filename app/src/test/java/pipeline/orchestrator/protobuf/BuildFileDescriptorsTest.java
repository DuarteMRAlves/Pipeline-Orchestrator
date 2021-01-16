package pipeline.orchestrator.protobuf;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.FileDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildFileDescriptorsTest {

    private static final String DESCRIPTOR_NAME_1 = "Descriptor 1";
    private static final String DESCRIPTOR_NAME_2 = "Descriptor 2";
    private static final String DESCRIPTOR_NAME_3 = "Descriptor 3";

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

    @Test
    public void testBuildFileDescriptors() throws Exception {
        // Given
        FileDescriptorSet descriptorSet = FileDescriptorSet.newBuilder()
                .addFile(DESCRIPTOR_PROTO_3)
                .addFile(DESCRIPTOR_PROTO_2)
                .addFile(DESCRIPTOR_PROTO_1)
                .build();

        // When
        ImmutableList<FileDescriptor> result = Descriptors.buildAllFrom(descriptorSet);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(el -> DESCRIPTOR_NAME_1.equals(el.getName())));
        assertTrue(result.stream().anyMatch(el -> DESCRIPTOR_NAME_2.equals(el.getName())));
        assertTrue(result.stream().anyMatch(el -> DESCRIPTOR_NAME_3.equals(el.getName())));
    }

    @Test
    public void testNoDescriptors() throws Exception {
        // Given
        FileDescriptorSet descriptorSet = FileDescriptorSet.newBuilder()
                .build();

        // When
        ImmutableList<FileDescriptor> result = Descriptors.buildAllFrom(descriptorSet);

        // Then
        assertEquals(0, result.size());
    }
}
