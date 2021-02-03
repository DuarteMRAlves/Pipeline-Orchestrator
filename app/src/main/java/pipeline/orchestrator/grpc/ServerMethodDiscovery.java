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
import pipeline.orchestrator.common.Conditions;
import pipeline.orchestrator.grpc.methods.FullMethodDescription;
import pipeline.orchestrator.grpc.reflection.ServerReflectionHelper;
import pipeline.orchestrator.grpc.reflection.UnableToListServicesException;
import pipeline.orchestrator.grpc.reflection.UnableToLookupService;
import pipeline.orchestrator.protobuf.Descriptors;

import java.util.List;
import java.util.function.Supplier;

public class ServerMethodDiscovery {

    private ServerMethodDiscovery() {}

    public static FullMethodDescription discoverSingleMethod(Channel channel)
        throws UnableToDiscoverMethodException {

        ServerReflectionHelper serverReflectionHelper =
                buildServerReflectionHelper(channel);
        try {
            ServiceDescriptor serviceDescriptor =
                    getServiceDescriptor(serverReflectionHelper);
            List<MethodDescriptor> methodDescriptors =
                    serviceDescriptor.getMethods();

            Conditions.checkState(
                    methodDescriptors.size() == 1,
                    newExceptionSupplier(
                            "Wrong number of methods: 1 expected but found %s",
                            methodDescriptors.size()));

            MethodDescriptor methodDescriptor = methodDescriptors.get(0);

            String methodFullName = Naming.methodFullName(
                    serviceDescriptor.getName(),
                    methodDescriptor.getName());

            return FullMethodDescription.newBuilder()
                    .setMethodDescriptor(methodDescriptor)
                    .setMethodFullName(methodFullName)
                    .build();
        } catch (UnableToListServicesException exception) {
            throw buildUnableToDiscoverMethodException(channel.authority(), exception);
        } catch (UnableToLookupService exception) {
            throw buildUnableToDiscoverMethodException(channel.authority(), exception);
        } catch (DescriptorValidationException exception) {
            throw buildUnableToDiscoverMethodException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new UnableToDiscoverMethodException("Interrupted", exception);
        }
    }

    public static FullMethodDescription discoverSingleMethod(Channel channel, String methodName)
            throws UnableToDiscoverMethodException {

        ServerReflectionHelper serverReflectionHelper = buildServerReflectionHelper(channel);
        try {
            ServiceDescriptor serviceDescriptor = getServiceDescriptor(serverReflectionHelper);

            MethodDescriptor methodDescriptor = serviceDescriptor.getMethods().stream()
                    .filter(descriptor -> methodName.equals(descriptor.getName()))
                    .findAny()
                    .orElseThrow(newExceptionSupplier("Method '%s' Not Found", methodName));

            String methodFullName = Naming.methodFullName(
                    serviceDescriptor.getName(),
                    methodDescriptor.getName());

            return FullMethodDescription.newBuilder()
                    .setMethodDescriptor(methodDescriptor)
                    .setMethodFullName(methodFullName)
                    .build();

        } catch (UnableToListServicesException exception) {
            throw buildUnableToDiscoverMethodException(channel.authority(), exception);
        } catch (UnableToLookupService exception) {
            throw buildUnableToDiscoverMethodException(channel.authority(), exception);
        } catch (DescriptorValidationException exception) {
            throw buildUnableToDiscoverMethodException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new UnableToDiscoverMethodException("Interrupted", exception);
        }
    }

    private static ServerReflectionHelper buildServerReflectionHelper(Channel channel) {
        return ServerReflectionHelper.newBuilder()
                .forChannel(channel)
                .build();
    }

    private static ServiceDescriptor getServiceDescriptor(ServerReflectionHelper serverReflectionHelper) throws
            UnableToListServicesException,
            InterruptedException,
            UnableToDiscoverMethodException,
            UnableToLookupService,
            DescriptorValidationException {
        String serviceName = findServiceName(serverReflectionHelper);

        FileDescriptorSet descriptorSet = serverReflectionHelper.lookupService(serviceName);

        ImmutableList<FileDescriptor> fileDescriptors = Descriptors.buildAllFrom(descriptorSet);

        return Descriptors.findService(
                serviceName,
                fileDescriptors)
                .orElseThrow(newExceptionSupplier("Service Not Found"));
    }

    private static String findServiceName(ServerReflectionHelper serverReflectionHelper)
            throws UnableToListServicesException, InterruptedException,
            UnableToDiscoverMethodException {
        ImmutableSet<String> services = serverReflectionHelper.listServices();

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

    private static Supplier<UnableToDiscoverMethodException> newExceptionSupplier(String message, Object... params) {
        return () -> new UnableToDiscoverMethodException(String.format(message, params));
    }

    private static UnableToDiscoverMethodException buildUnableToDiscoverMethodException(
            String authority,
            UnableToListServicesException exception) {
        return new UnableToDiscoverMethodException(
                String.format("Unable to list services at %s", authority),
                exception);
    }

    private static UnableToDiscoverMethodException buildUnableToDiscoverMethodException(
            String authority,
            UnableToLookupService exception) {
        return new UnableToDiscoverMethodException(
                String.format("Unable to lookup service at %s", authority),
                exception);
    }

    private static UnableToDiscoverMethodException buildUnableToDiscoverMethodException(
            DescriptorValidationException exception) {
        return new UnableToDiscoverMethodException(
                "Protobuf Descriptors Error",
                exception);
    }
}
