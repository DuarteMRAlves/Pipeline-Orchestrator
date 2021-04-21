package pipeline.orchestrator.reflection;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.grpc.Channel;
import pipeline.orchestrator.common.Conditions;
import pipeline.orchestrator.grpc.methods.FullMethodDescription;
import pipeline.orchestrator.grpc.utils.Naming;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ServerMethodDiscovery {

    private ServerMethodDiscovery() {}

    public static FullMethodDescription discoverMethod(
            Channel channel,
            MethodSearchInformation searchInformation
    ) throws UnableToDiscoverMethodException {

        try {
            ServiceDescriptor serviceDescriptor;
            Optional<String> serviceName = searchInformation.getServiceName();
            if (serviceName.isPresent()) {
                serviceDescriptor = new ServiceFinder()
                        .findServiceByName(channel, serviceName.get());
            } else {
                serviceDescriptor =
                        new ServiceFinder().findOnlyService(channel);
            }

            Optional<String> methodName = searchInformation.getMethodName();
            if (methodName.isPresent()) {
                return getMethodFromName(serviceDescriptor, methodName.get());
            } else {
                return getOnlyMethod(serviceDescriptor);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new UnableToDiscoverMethodException("Interrupted", exception);
        }
    }

    private static FullMethodDescription getMethodFromName(
            ServiceDescriptor serviceDescriptor,
            String methodName
    ) throws UnableToDiscoverMethodException {
        MethodDescriptor methodDescriptor =
                serviceDescriptor.getMethods().stream()
                        .filter(descriptor -> methodName.equals(descriptor.getName()))
                        .findAny()
                        .orElseThrow(newExceptionSupplier(
                                "Method '%s' Not Found",
                                methodName));

        String methodFullName = Naming.methodFullName(
                serviceDescriptor.getName(),
                methodDescriptor.getName());

        return FullMethodDescription.newBuilder()
                .setMethodDescriptor(methodDescriptor)
                .setMethodFullName(methodFullName)
                .build();
    }

    private static FullMethodDescription getOnlyMethod(
            ServiceDescriptor serviceDescriptor
    ) throws UnableToDiscoverMethodException {
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
    }

    private static Supplier<UnableToDiscoverMethodException> newExceptionSupplier(
            String message,
            Object... params
    ) {
        return () -> new UnableToDiscoverMethodException(String.format(message,
                                                                       params));
    }
}
