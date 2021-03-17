package pipeline.orchestrator.reflection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.grpc.Channel;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import pipeline.orchestrator.common.Conditions;
import pipeline.orchestrator.grpc.reflection.ServerReflectionHelper;
import pipeline.orchestrator.grpc.reflection.UnableToListServicesException;
import pipeline.orchestrator.grpc.reflection.UnableToLookupServiceException;
import pipeline.orchestrator.protobuf.Descriptors;

/**
 * Class to find the gRPC service that a stage
 * should call
 */
public class ServiceFinder {

    /**
     * Finds the service offered by the grpc server on the
     * other endpoint of the given channel.
     * The server should have reflection enabled and only one other server
     * @param channel channel to use
     * @return the descriptor for the grpc service
     * @throws UnableToDiscoverMethodException if an error occurred
     */
    public ServiceDescriptor findOnlyService(Channel channel)
            throws UnableToDiscoverMethodException, InterruptedException {

        ServerReflectionHelper helper = ServerReflectionHelper.newBuilder()
                .forChannel(channel)
                .build();

        String serviceName = findServiceName(channel, helper);

        return getServiceDescriptor(serviceName, channel, helper);
    }

    /**
     * Finds the service offered by the grpc server on the
     * other endpoint of the given channel.
     * The server should have reflection enabled and only one other server
     * @param channel channel to use
     * @param name name of the service to search
     * @return the descriptor for the grpc service
     * @throws UnableToDiscoverMethodException if an error occurred
     */
    public ServiceDescriptor findServiceByName(Channel channel, String name)
        throws UnableToDiscoverMethodException, InterruptedException {

        ServerReflectionHelper helper = ServerReflectionHelper.newBuilder()
                .forChannel(channel)
                .build();

        return getServiceDescriptor(name, channel, helper);
    }

    private String findServiceName(Channel channel, ServerReflectionHelper serverReflectionHelper)
            throws UnableToDiscoverMethodException, InterruptedException {

        try {

            ImmutableSet<String> servicesWithoutReflection = serverReflectionHelper.listServices().stream()
                    .filter(service -> !service.equals(ServerReflectionGrpc.SERVICE_NAME))
                    .collect(ImmutableSet.toImmutableSet());

            Conditions.checkState(servicesWithoutReflection.size() == 1,
                    () -> UnableToDiscoverMethodException.fromWrongNumberOfServices(1, servicesWithoutReflection.size()));

            return servicesWithoutReflection.stream()
                    .findAny()
                    // Should never happen since set has 1 element
                    .orElseThrow(IllegalStateException::new);
        }
        catch (UnableToListServicesException e) {
            throw UnableToDiscoverMethodException.fromUnableToListServicesException(channel.authority(), e);
        }
    }

    private ServiceDescriptor getServiceDescriptor(
            String serviceName,
            Channel channel,
            ServerReflectionHelper helper
    ) throws InterruptedException, UnableToDiscoverMethodException {

        try {

            FileDescriptorSet descriptorSet = helper.lookupService(serviceName);
            ImmutableList<FileDescriptor> fileDescriptors = Descriptors.buildAllFrom(descriptorSet);

            return Descriptors.findService(
                    serviceName,
                    fileDescriptors)
                    .orElseThrow(() -> new UnableToDiscoverMethodException("Service Not Found"));

        } catch (UnableToLookupServiceException e) {
            throw UnableToDiscoverMethodException.fromUnableToLookupServiceException(channel.authority(), e);
        } catch (DescriptorValidationException e) {
            throw UnableToDiscoverMethodException.fromDescriptorsValidationException(e);
        }
    }
}
