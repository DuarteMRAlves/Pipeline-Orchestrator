package pipeline.orchestrator.grpc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.grpc.Channel;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import pipeline.core.common.protobuf.Descriptors;
import pipeline.core.common.utils.Conditions;
import pipeline.core.discovery.ServerInfoProvider;
import pipeline.core.discovery.UnableToListServicesException;
import pipeline.core.discovery.UnableToLookupService;
import pipeline.core.invocation.Naming;

import java.util.List;
import java.util.function.Supplier;

public class ServerMethodDiscovery {

    private ServerMethodDiscovery() {}

    public static FullMethodDescription discoverSingleMethod(Channel channel)
        throws FailedToExecuteRequestException {
        ServerInfoProvider serverInfoProvider = buildServerInfoProvider(channel);
        try {
            ServiceDescriptor serviceDescriptor = getServiceDescriptor(serverInfoProvider);
            List<MethodDescriptor> methodDescriptors = serviceDescriptor.getMethods();

            Conditions.checkState(
                    methodDescriptors.size() == 1,
                    newExceptionSupplier(
                            "Wrong number of methods: 1 expected but found %s",
                            methodDescriptors.size()));

            MethodDescriptor methodDescriptor = methodDescriptors.get(0);

            String methodFullName = Naming.generateMethodFullName(
                    serviceDescriptor.getName(),
                    methodDescriptor.getName());

            return FullMethodDescription.newBuilder()
                    .setMethodDescriptor(methodDescriptor)
                    .setMethodFullName(methodFullName)
                    .build();

        } catch (DescriptorValidationException | UnableToListServicesException | UnableToLookupService exception) {
            throw new FailedToExecuteRequestException("Unable to discover method", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FailedToExecuteRequestException("Unable to discover method", exception);
        }
    }

    public static FullMethodDescription discoverSingleMethod(Channel channel, String methodName)
            throws FailedToExecuteRequestException {

        ServerInfoProvider serverInfoProvider = buildServerInfoProvider(channel);
        try {
            ServiceDescriptor serviceDescriptor = getServiceDescriptor(serverInfoProvider);

            MethodDescriptor methodDescriptor = serviceDescriptor.getMethods().stream()
                    .filter(descriptor -> methodName.equals(descriptor.getName()))
                    .findAny()
                    .orElseThrow(newExceptionSupplier("Method '%s' Not Found", methodName));

            String methodFullName = Naming.generateMethodFullName(
                    serviceDescriptor.getName(),
                    methodDescriptor.getName());

            return FullMethodDescription.newBuilder()
                    .setMethodDescriptor(methodDescriptor)
                    .setMethodFullName(methodFullName)
                    .build();

        } catch (DescriptorValidationException | UnableToListServicesException | UnableToLookupService exception) {
            throw new FailedToExecuteRequestException("Unable to discover method", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FailedToExecuteRequestException("Unable to discover method", exception);
        }
    }

    private static ServerInfoProvider buildServerInfoProvider(Channel channel) {
        return ServerInfoProvider.newBuilder()
                .forChannel(channel)
                .build();
    }

    private static ServiceDescriptor getServiceDescriptor(ServerInfoProvider serverInfoProvider) throws
            UnableToListServicesException,
            InterruptedException,
            FailedToExecuteRequestException,
            UnableToLookupService,
            DescriptorValidationException {
        String serviceName = findServiceName(serverInfoProvider);

        FileDescriptorSet descriptorSet = serverInfoProvider.lookupService(serviceName);

        ImmutableList<FileDescriptor> fileDescriptors = Descriptors.buildAllFrom(descriptorSet);

        return Descriptors.findService(
                serviceName,
                fileDescriptors)
                .orElseThrow(newExceptionSupplier("Service Not Found"));
    }

    private static String findServiceName(ServerInfoProvider serverInfoProvider)
            throws UnableToListServicesException, InterruptedException,
            FailedToExecuteRequestException {
        ImmutableSet<String> services = serverInfoProvider.listServices();

        Conditions.checkState(services.size() == 2,
                newExceptionSupplier(
                        "Wrong number of services: 2 expected but found %d",
                        services.size()));

        return services.stream()
                .filter(service -> !service.equals(ServerReflectionGrpc.SERVICE_NAME))
                .findAny()
                .orElseThrow(newExceptionSupplier(
                        "Invalid services: Expected service different from gRPC Reflection Service"));
    }

    private static Supplier<FailedToExecuteRequestException> newExceptionSupplier(String message, Object... params) {
        return () -> new FailedToExecuteRequestException(String.format(message, params));
    }
}
