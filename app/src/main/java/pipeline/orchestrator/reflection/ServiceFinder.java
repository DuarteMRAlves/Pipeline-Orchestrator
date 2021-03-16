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
import pipeline.orchestrator.grpc.reflection.UnableToLookupService;
import pipeline.orchestrator.protobuf.Descriptors;

import java.util.HashSet;
import java.util.Set;

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

        try {
            String serviceName = findServiceName(helper);

            FileDescriptorSet descriptorSet = helper.lookupService(serviceName);

            ImmutableList<FileDescriptor> fileDescriptors = Descriptors.buildAllFrom(descriptorSet);

            return Descriptors.findService(
                    serviceName,
                    fileDescriptors)
                    .orElseThrow(() -> new UnableToDiscoverMethodException("Service Not Found"));
        } catch (UnableToListServicesException e) {
            throw UnableToDiscoverMethodException.fromUnableToListServicesException(channel.authority(), e);
        } catch (UnableToLookupService e) {
            throw UnableToDiscoverMethodException.fromUnableToLookupServiceException(channel.authority(), e);
        } catch (DescriptorValidationException e) {
            throw UnableToDiscoverMethodException.fromDescriptorsValidationException(e);
        }
    }

    private String findServiceName(ServerReflectionHelper serverReflectionHelper)
            throws UnableToDiscoverMethodException, InterruptedException, UnableToListServicesException {

        ImmutableSet<String> services = serverReflectionHelper.listServices();

        Set<String> servicesWithoutReflection = new HashSet<>(services);
        servicesWithoutReflection.remove(ServerReflectionGrpc.SERVICE_NAME);

        Conditions.checkState(servicesWithoutReflection.size() == 1,
                () -> UnableToDiscoverMethodException.fromWrongNumberOfServices(1, servicesWithoutReflection.size()));

        return servicesWithoutReflection.stream()
                .findAny()
                // Should never happen since set has 1 element
                .orElseThrow(IllegalStateException::new);
    }
}
