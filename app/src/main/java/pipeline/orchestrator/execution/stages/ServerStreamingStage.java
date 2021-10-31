package pipeline.orchestrator.execution.stages;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.execution.stages.events.UnavailableServiceEvent;
import pipeline.orchestrator.grpc.methods.FullMethodDescription;
import pipeline.orchestrator.grpc.utils.StatusRuntimeExceptions;
import pipeline.orchestrator.grpc.methods.AsyncServerStreamingMethodInvoker;

import java.util.concurrent.CountDownLatch;

/**
 * Stage that executes a Server Streaming Grpc Method
 * This stage can only be in the beginning of the pipeline
 * since it creates its own ids for the computational states
 */
public class ServerStreamingStage extends ExecutionStage {

    private int currentId = 0;

    private final AsyncServerStreamingMethodInvoker<DynamicMessage, DynamicMessage> invoker;

    private boolean paused = false;
    private boolean finished = false;

    private ServerStreamingStage(
            String stageName,
            Channel channel,
            FullMethodDescription fullMethodDescription,
            EventBus eventBus) {

        super(stageName,  channel, fullMethodDescription, eventBus);
        invoker = AsyncServerStreamingMethodInvoker.<DynamicMessage, DynamicMessage>newBuilder()
                .forChannel(getChannel())
                .forMethod(buildGrpcMethodDescriptor())
                .build();
    }

    @Override
    public void run() {
        StageInputStream inputStream = getStageInputStream();
        StageOutputStream outputStream = getStageOutputStream();

        getLogger().debug("Stage '{}': Running", getName());

        // Check if the input stream is a source so that it can ignore ids
        Preconditions.checkState(inputStream.isSource());

        // Run forever until finished
        while (true) {

            waitPaused();

            // Check if finished while paused
            if (isFinished()) {
                break;
            }

            ComputationState requestState = inputStream.get();
            CountDownLatch streamEnd = new CountDownLatch(1);

            invoker.call(requestState.getMessage(), new StreamObserver<>() {
                @Override
                public void onNext(DynamicMessage value) {
                    ComputationState state = ComputationState.from(
                            currentId++,
                            value);
                    outputStream.accept(state);
                }

                @Override
                public void onError(Throwable t) {
                    handleThrowable(t);
                }

                @Override
                public void onCompleted() {
                    streamEnd.countDown();
                }
            });

            try {
                // Wait for the stream to end before starting the next stream
                streamEnd.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (Thread.currentThread().isInterrupted()) {
                pause();
            }
        }
        getLogger().info("Stage '{}': Processing finished", getName());
    }

    @Override
    public void resume() {
        getLogger().trace("Stage '{}': Received resume signal", getName());
        synchronized (this) {
            // Can only resume if not paused
            if (!finished) {
                paused = false;
                // Notify thread to resume if waiting
                notifyAll();
            }
            else {
                // Stage remains finished
                // Should not happen
                getLogger().warn(
                        "Stage '{}': Not resumed (already finished)",
                        getName());
            }
        }
    }

    @Override
    public void pause() {
        getLogger().trace("Stage '{}': Received pause signal", getName());
        synchronized (this) {
            // Can only pause if not paused
            if (!finished) {
                // In this case of the stream it will keep processing the
                // received objects and storing them if possible
                paused = true;
            }
            else {
                // Stage remains finished
                // Should not happen
                getLogger().warn(
                        "Stage '{}': Not paused (already finished)",
                        getName());
            }
        }
    }

    @Override
    public void finish() {
        getLogger().trace("Stage '{}': Received finish signal", getName());
        synchronized (this) {
            // Unpause in order to finish
            paused = false;
            finished = true;
            notifyAll();
        }
    }

    /**
     * Returns a builder for server streaming pipeline stages
     * @return the new builder
     */
    public static ExecutionStageBuilder<ServerStreamingStage> newBuilder() {
        return new Builder();
    }

    private void waitPaused() {
        synchronized (this) {
            while (paused) {
                getLogger().trace("Stage '{}': Waiting", getName());
                try {
                    wait();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private boolean isFinished() {
        synchronized (this) {
            return finished;
        }
    }

    private void handleThrowable(Throwable t) {
        if (t instanceof StatusRuntimeException) {
            handleStatusRuntimeException((StatusRuntimeException) t);
            pause();
        }
        else {
            getLogger().error("Unknown Throwable when executing call", t);
            System.exit(1);
        }
    }

    private void handleStatusRuntimeException(StatusRuntimeException e) {
        if (StatusRuntimeExceptions.isUnavailable(e)) {
            postEvent(new UnavailableServiceEvent(getName()));
        }
        else {
            getLogger().error(
                    "Unknown StatusRuntimeException when executing call",
                    e);
            System.exit(1);
        }
    }

    static final class Builder extends
            ExecutionStageBuilder<ServerStreamingStage> {

        @Override
        public ServerStreamingStage build() {
            return new ServerStreamingStage(
                    getName(),
                    getChannel(),
                    getDescription(),
                    getEventBus());
        }
    }
}
