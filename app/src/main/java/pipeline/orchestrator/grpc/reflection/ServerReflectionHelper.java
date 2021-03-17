package pipeline.orchestrator.grpc.reflection;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.grpc.Channel;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.reflection.v1alpha.ServiceResponse;
import pipeline.orchestrator.protobuf.DescriptorProtos;
import pipeline.orchestrator.common.Conditions;
import pipeline.orchestrator.common.Futures;

import java.util.*;

import static io.grpc.reflection.v1alpha.ServerReflectionResponse.MessageResponseCase.LIST_SERVICES_RESPONSE;
import static io.grpc.reflection.v1alpha.ServerReflectionResponse.MessageResponseCase.FILE_DESCRIPTOR_RESPONSE;

/**
 * Class to help with the discovery of information
 * regarding the available services at a grpc server
 * as well as information about those services such as
 * method information
 */
public class ServerReflectionHelper {

    private static final String DEFAULT_HOST = "localhost";

    private String host;
    private Channel channel;

    private ServerReflectionHelper() {}

    /**
     * Lists the available services at the server
     * on the server-side endpoint of the channel
     * @return the names of the available services
     * @throws UnableToListServicesException if an error occurs
     * @throws InterruptedException if the thread is interrupted
     *                              while waiting for a reply from the server
     */
    public ImmutableSet<String> listServices()
            throws UnableToListServicesException, InterruptedException {

        ReflectionStreamManager manager = newReflectionStreamManager();
        try {
            ServerReflectionRequest request =
                    ReflectionRequestFactory.listServicesRequest(host);

            ServerReflectionResponse response
                    = Futures.waitComputation(manager.submit(request),
                    UnableToListServicesException::new);

            Conditions.checkState(
                    response.getMessageResponseCase() == LIST_SERVICES_RESPONSE,
                    UnableToListServicesException::new);

            return response.getListServicesResponse().getServiceList().stream()
                    .map(ServiceResponse::getName)
                    .collect(ImmutableSet.toImmutableSet());
        }
        finally {
            // Complete to close connection so that channel can be shutdown
            manager.complete();
        }
    }

    /**
     * Method to collect the necessary descriptors for a given
     * service, including all its methods and messages
     * @param serviceName the name of the service
     * @return a set of file descriptors with all the necessary
     *         descriptors for the methods and messages of the service
     * @throws UnableToLookupServiceException if an error occurs
     * @throws InterruptedException if the thread is interrupted
     *                              while waiting for a reply from the server
     */
    public FileDescriptorSet lookupService(String serviceName)
            throws UnableToLookupServiceException, InterruptedException {

        ReflectionStreamManager manager = newReflectionStreamManager();
        try {
            LookupServiceHelper helper = new LookupServiceHelper(serviceName, manager);
            Iterable<FileDescriptorProto> fileDescriptorProtos = helper.lookupService();
            return DescriptorProtos.buildSetFromProtos(fileDescriptorProtos);
        }
        finally {
            // Complete to close connection so that channel can be shutdown
            manager.complete();
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String host = DEFAULT_HOST;
        private Channel channel;

        public Builder forChannel(Channel channel) {
            this.channel = channel;
            return this;
        }

        public Builder forHost(String host) {
            this.host = host;
            return this;
        }

        public ServerReflectionHelper build() {
            ServerReflectionHelper provider = new ServerReflectionHelper();
            provider.channel = channel;
            provider.host = host;
            return provider;
        }
    }

    private ReflectionStreamManager newReflectionStreamManager() {
        return ReflectionStreamManager.newBuilder()
                .forChannel(channel)
                .build();
    }

    /**
     * Class to manage a lookup request
     * If will submit the initial request for the
     * protobuf file with the service name and issue
     * subsequent requests with the file dependencies
     * until all dependencies are collected
     */
    private class LookupServiceHelper {

        Queue<ServerReflectionRequest> pendingRequests = new LinkedList<>();
        Map<String, FileDescriptorProto> resolvedFileDescriptors = new HashMap<>();
        Set<String> pendingDependencies = new HashSet<>();
        String serviceName;
        ReflectionStreamManager manager;

        private LookupServiceHelper(String serviceName, ReflectionStreamManager manager) {
            this.serviceName = serviceName;
            this.manager = manager;
        }

        private Iterable<FileDescriptorProto> lookupService()
                throws UnableToLookupServiceException, InterruptedException {
            pendingRequests.add(ReflectionRequestFactory.fileBySymbolRequest(host, serviceName));

            while (!pendingRequests.isEmpty()) {
                ServerReflectionResponse response =
                        Futures.waitComputation(manager.submit(pendingRequests.poll()),
                                UnableToLookupServiceException::new);
                Conditions.checkState(
                        response.getMessageResponseCase() == FILE_DESCRIPTOR_RESPONSE,
                        UnableToLookupServiceException::new);

                List<ByteString> fileDescriptorsBytes =
                        response.getFileDescriptorResponse().getFileDescriptorProtoList();
                processFileDescriptors(DescriptorProtos.parse(fileDescriptorsBytes));
            }
            return resolvedFileDescriptors.values();
        }

        private void processFileDescriptors(ImmutableSet<FileDescriptorProto> fileDescriptors) {
            fileDescriptors.forEach(fileDescriptorProto -> {
                resolvedFileDescriptors.put(fileDescriptorProto.getName(), fileDescriptorProto);
                pendingDependencies.remove(fileDescriptorProto.getName());
                fileDescriptorProto.getDependencyList().forEach(dependency -> {
                    if (!(pendingDependencies.contains(dependency) && resolvedFileDescriptors.containsKey(dependency))) {
                        pendingDependencies.add(dependency);
                        pendingRequests.add(ReflectionRequestFactory.fileByFilenameRequest(host, dependency));
                    }
                });
            });
        }
    }
}

