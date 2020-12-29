package pipeline.orchestrator.execution.stages;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pipeline.core.common.grpc.StatusRuntimeExceptions;
import pipeline.core.invocation.AsyncServerStreamingMethodInvoker;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.execution.stages.events.UnavailableServiceEvent;
import pipeline.orchestrator.grpc.FullMethodDescription;

import java.util.concurrent.CountDownLatch;

/**
 * Stage that executes a Server Streaming Grpc Method
 * This stage can only be in the beginning of the pipeline
 * since it creates its own ids for the computational states
 */
public class ServerStreamingPipelineStage extends AbstractPipelineStage {

    private int currentId = 0;

    private final AsyncServerStreamingMethodInvoker<DynamicMessage, DynamicMessage> invoker;

    private boolean running = true;

    ServerStreamingPipelineStage(
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

        // Check if the input stream is a source so that it can ignore ids
        Preconditions.checkState(inputStream.isSource());

        while (isRunning()) {
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
                pauseStage();
            }
        }
        getLogger().info("{}: Stage processing finished", getName());
    }

    /**
     * Pauses the execution of the stage
     */
    @Override
    public void pauseStage() {
        synchronized (this) {
            running = false;
        }
    }

    private boolean isRunning() {
        synchronized (this) {
            return running;
        }
    }

    private void handleThrowable(Throwable t) {
        if (StatusRuntimeExceptions.isInstance(t)) {
            handleStatusRuntimeException((StatusRuntimeException) t);
            pauseStage();
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
            getLogger().error("Unknown StatusRuntimeException when executing call", e);
            System.exit(1);
        }
    }
}
