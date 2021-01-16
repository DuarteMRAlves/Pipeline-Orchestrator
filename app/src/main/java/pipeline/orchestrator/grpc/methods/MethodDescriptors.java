package pipeline.orchestrator.grpc.methods;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import io.grpc.MethodDescriptor.MethodType;
import pipeline.orchestrator.grpc.messages.DynamicMessageMarshaller;

/**
 * Helper static class for handling method descriptors
 */
public class MethodDescriptors {

    private MethodDescriptors() {}

    /**
     * Builds a new method descriptor that can be invoked
     * using instances of {@link DynamicMessage}s
     * @param description full description for the given method
     * @return the new method descriptor
     */
    public static io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> fromDescription(
           FullMethodDescription description) {
        return fromParts(
                description.getMethodFullName(),
                getType(description.getMethodDescriptor()),
                description.getMethodDescriptor().getInputType(),
                description.getMethodDescriptor().getOutputType());
    }

    /**
     * Finds the method type from its methodDescriptor
     * @param methodDescriptor method descriptor to analyse
     * @return the method type for the given descriptor
     */
    public static MethodType getType(MethodDescriptor methodDescriptor) {
        boolean serverStreaming = methodDescriptor.isServerStreaming();
        boolean clientStreaming = methodDescriptor.isClientStreaming();
        if (serverStreaming) {
            if (clientStreaming) {
                return MethodType.BIDI_STREAMING;
            } else {
                return MethodType.SERVER_STREAMING;
            }
        }
        else {
            if (clientStreaming) {
                return MethodType.CLIENT_STREAMING;
            } else {
                return MethodType.UNARY;
            }
        }
    }

    /**
     * Builds a new method descriptor that can be invoked
     * using instances of {@link DynamicMessage}s
     * @param fullMethodName full name for the method
     * @param type method type
     * @param requestDescriptor descriptor for the method requests
     * @param responseDescriptor descriptor for the method responses
     * @return the new method descriptor
     */
    private static io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> fromParts(
            String fullMethodName,
            MethodType type,
            Descriptors.Descriptor requestDescriptor,
            Descriptors.Descriptor responseDescriptor) {

        DynamicMessageMarshaller requestMarshaller =
                DynamicMessageMarshaller.forDescriptor(requestDescriptor);
        DynamicMessageMarshaller responseMarshaller =
                DynamicMessageMarshaller.forDescriptor(responseDescriptor);
        return io.grpc.MethodDescriptor.newBuilder(requestMarshaller, responseMarshaller)
                .setFullMethodName(fullMethodName)
                .setType(type)
                .build();
    }
}
