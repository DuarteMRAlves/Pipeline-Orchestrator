package pipeline.orchestrator.protobuf;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.*;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.common.Conditions;

import java.util.List;
import java.util.Optional;

/**
 * Helper static class to manage descriptor protos
 */
public class DescriptorProtos {

    private DescriptorProtos() {}

    private static final Logger LOGGER = LogManager.getLogger(DescriptorProtos.class);

    /**
     * Build a set of file descriptors from the protos
     * @param fileDescriptors set of DescriptorProtos for the files
     * @return the set of file descriptors
     */
    public static FileDescriptorSet buildSetFromProtos(
            Iterable<FileDescriptorProto> fileDescriptors) {

        return FileDescriptorSet.newBuilder().addAllFile(fileDescriptors).build();
    }

    /**
     * Build a set of file descriptors protos from a list
     * of serialized protos in byte strings
     * @param fileDescriptors list of serialized protos
     * @return the set of file descriptors protos
     */
    public static ImmutableSet<FileDescriptorProto> parse(List<ByteString> fileDescriptors) {
        return fileDescriptors.stream()
                .map(DescriptorProtos::parse)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * Finds a service by its name in a file descriptor set
     * @param fileDescriptorSet file descriptor set to search
     * @param serviceName name of the service to find
     * @return the proto for the service (may be empty if not found)
     */
    public static Optional<ServiceDescriptorProto> findService(
            FileDescriptorSet fileDescriptorSet,
            String serviceName) {

        return fileDescriptorSet.getFileList().stream()
                .flatMap(file -> file.getServiceList().stream())
                .filter(serviceDescriptorProto -> serviceDescriptorProto.getName().equals(serviceName))
                .findAny();
    }

    /**
     * Finds a method by its name in a service descriptor proto
     * @param serviceDescriptorProto service descriptor proto to search
     * @param methodName name of the method
     * @return the proto for the method (may be empty if not found)
     */
    public static Optional<MethodDescriptorProto> findMethod(
            ServiceDescriptorProto serviceDescriptorProto,
            String methodName) {
        return serviceDescriptorProto.getMethodList().stream()
                .filter(methodDescriptorProto -> methodDescriptorProto.getName().equals(methodName))
                .findAny();
    }

    /**
     * Finds a message type by its name in a file descriptor set
     * @param fileDescriptorSet set of file descriptors to search
     * @param messageTypeName name of the message type
     * @return the proto for the file (may be empty if not found)
     */
    public static Optional<DescriptorProto> findMessageType(
            FileDescriptorSet fileDescriptorSet,
            String messageTypeName) {

        return fileDescriptorSet.getFileList().stream()
                .flatMap(file -> file.getMessageTypeList().stream())
                .filter(message -> messageTypeName.equals(message.getName()))
                .findAny();
    }

    /**
     * Build the dependency graph of file descriptors for a given
     * file descriptor set
     * @param fileDescriptorSet file descriptor set to build the graph from
     * @return a DAG with nodes as the descriptors and the edges
     *         as dependencies
     */
    public static ImmutableGraph<FileDescriptorProto> buildDependencyGraph(
            FileDescriptorSet fileDescriptorSet) {

        MutableGraph<FileDescriptorProto> dependenciesGraph =
                buildFileDescriptorsGraph(fileDescriptorSet);

        ImmutableMap<String, FileDescriptorProto> knownDescriptors =
                buildFileDescriptorsMapping(fileDescriptorSet);

        fileDescriptorSet.getFileList().forEach(descriptorProto -> processFileDescriptor(dependenciesGraph, knownDescriptors, descriptorProto));

        Conditions.checkState(
                !Graphs.hasCycle(dependenciesGraph),
                CircularDependenciesException::new);

        return ImmutableGraph.copyOf(dependenciesGraph);
    }

    /**
     * Build a map between a file name and the DescriptorProto for the file
     * @param fileDescriptorSet set of files to build the map from
     * @return a immutable map indexed by file name with
     *         the respective DescriptorProtos
     */
    public static ImmutableMap<String, FileDescriptorProto> buildFileDescriptorsMapping(
            FileDescriptorSet fileDescriptorSet) {

        ImmutableMap.Builder<String, FileDescriptorProto> mapBuilder =
                ImmutableMap.builder();

        fileDescriptorSet.getFileList().forEach(fileDescriptorProto ->
                mapBuilder.put(fileDescriptorProto.getName(), fileDescriptorProto));

        return mapBuilder.build();
    }

    private static MutableGraph<FileDescriptorProto> buildFileDescriptorsGraph(
            FileDescriptorSet fileDescriptorSet) {

        return GraphBuilder.directed()
                .allowsSelfLoops(false)
                .expectedNodeCount(fileDescriptorSet.getFileCount())
                .build();
    }

    private static void processFileDescriptor(
            MutableGraph<FileDescriptorProto> dependenciesGraph,
            ImmutableMap<String, FileDescriptorProto> knownDescriptors,
            FileDescriptorProto descriptorProto) {

        dependenciesGraph.addNode(descriptorProto);
        descriptorProto.getDependencyList().forEach(dependencyName -> {
            FileDescriptorProto dependency = knownDescriptors.get(dependencyName);
            Conditions.checkState(dependency != null, () -> new MissingDependencyException(dependencyName));
            dependenciesGraph.putEdge(descriptorProto, dependency);
        });
    }

    private static Optional<FileDescriptorProto> parse(ByteString fileDescriptor) {
        try {
            return Optional.of(FileDescriptorProto.parseFrom(fileDescriptor));
        } catch (InvalidProtocolBufferException e) {
            LOGGER.warn("Unable to parse file descriptor", e);
            return Optional.empty();
        }
    }
}
