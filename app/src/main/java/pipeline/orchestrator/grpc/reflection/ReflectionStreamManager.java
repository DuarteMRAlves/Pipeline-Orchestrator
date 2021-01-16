package pipeline.orchestrator.grpc.reflection;

import com.google.common.util.concurrent.SettableFuture;
import io.grpc.Channel;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc.ServerReflectionStub;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Class to submit server reflection requests in an asynchronous way
 * Handles one bidirectional stream between the client and the server
 * Does not close the channel. Only handles one stream of requests.
 */
public class ReflectionStreamManager {

    private static final int DEFAULT_SUBMITTED_REQUESTS_BUFFER_SIZE = 16;

    private Channel channel = null;
    private ServerReflectionStub stub = null;

    private StreamObserver<ServerReflectionRequest> requestStreamObserver;

    private int maxSimultaneousRequests = DEFAULT_SUBMITTED_REQUESTS_BUFFER_SIZE;
    private int simultaneousRequests = 0;
    private Queue<SettableFuture<ServerReflectionResponse>> waitingResponses;

    private ReflectionStreamManager() {}

    /**
     * Submits the method to be processed if the requests buffer is not full
     * @param request to be processed
     * @return future with the response to the given request
     */
    public Future<ServerReflectionResponse> submit(ServerReflectionRequest request) {
        checkState(simultaneousRequests < maxSimultaneousRequests);

        SettableFuture<ServerReflectionResponse> future = SettableFuture.create();
        waitingResponses.add(future);
        simultaneousRequests++;
        this.requestStreamObserver.onNext(request);

        return future;
    }

    /**
     * Ends the stream of requests
     */
    public void complete() { this.requestStreamObserver.onCompleted(); }

    public static ReflectionStreamManager.Builder newBuilder() {
        return new ReflectionStreamManager.Builder();
    }

    private class ExecutorStreamObserver implements StreamObserver<ServerReflectionResponse> {

        @Override
        public void onNext(ServerReflectionResponse response) {
            checkState(simultaneousRequests > 0);

            SettableFuture<ServerReflectionResponse> future = waitingResponses.poll();
            assert future != null; // Should never happen
            future.set(response);
        }

        @Override
        public void onError(Throwable t) {
            waitingResponses.forEach(future -> future.setException(t));
            waitingResponses.clear();
        }

        @Override
        public void onCompleted() {
            if (!waitingResponses.isEmpty()) {
                waitingResponses.forEach(this::setStreamUnexpectedCompletionException);
            }
        }

        private void setStreamUnexpectedCompletionException(SettableFuture<?> future) {
            future.setException(
                    new ReflectionStreamUnexpectedCompletionException(
                            "Unexpected completion of server reflection stream"));
        }
    }

    public static class Builder {

        private final ReflectionStreamManager executor = new ReflectionStreamManager();

        public ReflectionStreamManager.Builder forChannel(Channel channel) {
            executor.channel = channel;
            return this;
        }

        public ReflectionStreamManager.Builder forMaxSimultaneousRequests(int maxSimultaneousRequests) {
            executor.maxSimultaneousRequests = maxSimultaneousRequests;
            return this;
        }

        public ReflectionStreamManager build() {
            checkNotNull(executor.channel, "Unable to build executor: Require channel");
            init();
            return executor;
        }

        private void init() {
            executor.stub = ServerReflectionGrpc.newStub(executor.channel);
            executor.requestStreamObserver = executor.stub.serverReflectionInfo(executor.new ExecutorStreamObserver());
            executor.waitingResponses = new ArrayBlockingQueue<>(executor.maxSimultaneousRequests);
        }
    }
}
