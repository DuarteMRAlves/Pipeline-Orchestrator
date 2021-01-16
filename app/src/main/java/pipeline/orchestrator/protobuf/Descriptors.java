package pipeline.orchestrator.protobuf;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.graph.ImmutableGraph;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.*;
import pipeline.orchestrator.common.DirectedGraphs;
import pipeline.orchestrator.common.Iterables;

import java.util.ArrayList;
import java.util.Optional;

public class Descriptors {

    private Descriptors() {}

    /**
     * Builds a list of file descriptors
     * @param fileDescriptorSet set of file descriptor protos
     * @return list of file descriptors
     * @throws DescriptorValidationException if a not valid descriptor is found
     */
    public static ImmutableList<FileDescriptor> buildAllFrom(
            FileDescriptorSet fileDescriptorSet)
            throws DescriptorValidationException {

        ImmutableGraph<FileDescriptorProto> dependenciesGraph = DescriptorProtos.buildDependencyGraph(fileDescriptorSet);
        // By processing the dependencies in reversed topological order, when we reach a file descriptor,
        // it's dependencies were already processed
        ImmutableList<FileDescriptorProto> reversedDependencies = DirectedGraphs.reversedTopologicalOrder(dependenciesGraph);

        ArrayList<FileDescriptor> descriptors = new ArrayList<>();

        for (FileDescriptorProto descriptorProto : reversedDependencies) {
            // Use previous descriptors as dependencies
            descriptors.add(FileDescriptor.buildFrom(descriptorProto, descriptors.toArray(new FileDescriptor[0])));
        }

        return ImmutableList.copyOf(descriptors);
    }


    public static ImmutableMap<String, FieldDescriptor> getSubMessagesFieldDescriptors(
            Descriptor descriptor) {

        ImmutableMap.Builder<String, FieldDescriptor> builder = ImmutableMap.builder();
        descriptor.getFields().forEach(fieldDescriptor -> {
            if (isMessage(fieldDescriptor)) {
                builder.put(fieldDescriptor.getName(), fieldDescriptor);
            }
        });
        return builder.build();
    }

    public static boolean isMessage(FieldDescriptor fieldDescriptor) {
        return fieldDescriptor.getJavaType().equals(FieldDescriptor.JavaType.MESSAGE);
    }

    public static Optional<ServiceDescriptor> findService(String serviceName, Iterable<FileDescriptor> fileDescriptors) {
        Preconditions.checkNotNull(serviceName);
        return Iterables.findFirstNonNull(fileDescriptors,
                fileDescriptor -> fileDescriptor.findServiceByName(serviceName));
    }
}
