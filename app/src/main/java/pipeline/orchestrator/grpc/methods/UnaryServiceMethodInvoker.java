package pipeline.orchestrator.grpc.methods;

import com.google.common.base.Preconditions;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;


/**
 * Class to invoke a unary service method using a managed channel
 * @param <ReqT> Type of the request
 * @param <RespT> Type of the response
 */
public class UnaryServiceMethodInvoker<ReqT, RespT> {

    private final Channel channel;
    private final MethodDescriptor<ReqT, RespT> methodDescriptor;
    private final CallOptions callOptions;

    private UnaryServiceMethodInvoker(Channel channel, MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions) {
        this.channel = channel;
        this.methodDescriptor = methodDescriptor;
        this.callOptions = callOptions;
    }

    public RespT call(ReqT request) {
        return ClientCalls.blockingUnaryCall(channel, methodDescriptor, callOptions, request);
    }

    public static <ReqT, ResT> Builder<ReqT, ResT> newBuilder() {
        return new Builder<>();
    }

    public static class Builder<ReqT, RespT> {

        private Channel channel;
        private MethodDescriptor<ReqT, RespT> methodDescriptor;
        private CallOptions callOptions = CallOptions.DEFAULT;

        public Builder<ReqT, RespT> forChannel(Channel channel) {
            this.channel = channel;
            return this;
        }

        public Builder<ReqT, RespT> forMethod(MethodDescriptor<ReqT, RespT> methodDescriptor) {
            Preconditions.checkArgument(methodDescriptor.getType() == MethodDescriptor.MethodType.UNARY,
                                        "Unsupported method type");
            this.methodDescriptor = methodDescriptor;
            return this;
        }

        public Builder<ReqT, RespT> withCallOptions(CallOptions callOptions) {
            this.callOptions = callOptions;
            return this;
        }

        public UnaryServiceMethodInvoker<ReqT, RespT> build() {
            UnaryServiceMethodInvoker<ReqT, RespT> invoker;
            if (methodDescriptor.getType() == MethodDescriptor.MethodType.UNARY) {
                invoker = new UnaryServiceMethodInvoker<>(channel, methodDescriptor, callOptions);
            } else {
                throw new IllegalStateException("Unknown type for method descriptor");
            }
            return invoker;
        }
    }
}
