package pipeline.orchestrator.grpc.methods;

import com.google.common.base.Preconditions;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

/**
 * Class to invoke a server streaming service method using a managed channel
 * @param <ReqT> Type of the request
 * @param <RespT> Type of the response
 */
public class AsyncServerStreamingMethodInvoker<ReqT, RespT> {

    private final Channel channel;
    private final MethodDescriptor<ReqT, RespT> methodDescriptor;
    private final CallOptions callOptions;

    private AsyncServerStreamingMethodInvoker(
            Channel channel,
            MethodDescriptor<ReqT, RespT> methodDescriptor,
            CallOptions callOptions) {
        this.channel = channel;
        this.methodDescriptor = methodDescriptor;
        this.callOptions = callOptions;
    }

    public void call(ReqT request, StreamObserver<RespT> streamObserver) {
        ClientCalls.asyncServerStreamingCall(createCall(), request, streamObserver);
    }

    private ClientCall<ReqT, RespT> createCall() {
        return channel.newCall(methodDescriptor, callOptions);
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
            Preconditions.checkArgument(
                    methodDescriptor.getType() == MethodDescriptor.MethodType.SERVER_STREAMING,
                    "Unsupported method type");
            this.methodDescriptor = methodDescriptor;
            return this;
        }

        public Builder<ReqT, RespT> withCallOptions(CallOptions callOptions) {
            this.callOptions = callOptions;
            return this;
        }

        public AsyncServerStreamingMethodInvoker<ReqT, RespT> build() {
            Preconditions.checkNotNull(channel);
            Preconditions.checkNotNull(methodDescriptor);
            return new AsyncServerStreamingMethodInvoker<>(
                    channel,
                    methodDescriptor,
                    callOptions);
        }

    }
}
