package pipeline.orchestrator.execution.stages;

import com.google.common.eventbus.EventBus;
import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.execution.stages.events.UnavailableServiceEvent;
import pipeline.orchestrator.grpc.methods.FullMethodDescription;
import pipeline.orchestrator.grpc.StatusRuntimeExceptions;
import pipeline.orchestrator.grpc.methods.UnaryServiceMethodInvoker;

/**
 * Stage that executes an Unary Grpc Method
 */
public class UnaryPipelineStage extends AbstractPipelineStage {

    private final UnaryServiceMethodInvoker<DynamicMessage, DynamicMessage> invoker;

    private boolean paused = false;
    private boolean finished = false;

    UnaryPipelineStage(
            String stageName,
            Channel channel,
            FullMethodDescription fullMethodDescription,
            EventBus eventBus) {

        super(stageName,  channel, fullMethodDescription, eventBus);
        invoker = buildInvoker();
    }

    @Override
    public void run() {
        StageInputStream inputStream = getStageInputStream();
        StageOutputStream outputStream = getStageOutputStream();

        // Run forever until finished
        while (true) {

            waitPaused();

            // Check if finished while paused
            if (isFinished()) {
                break;
            }

            ComputationState requestState = inputStream.get();
            try {
                DynamicMessage response = invoker.call(requestState.getMessage());
                ComputationState responseState = ComputationState.from(
                        requestState,
                        response);
                outputStream.accept(responseState);
            } catch (StatusRuntimeException e) {
                handleStatusRuntimeException(e);
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

    private void waitPaused() {
        synchronized (this) {
            while (paused) {
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

    private void handleStatusRuntimeException(StatusRuntimeException e) {
        if (StatusRuntimeExceptions.isUnavailable(e)) {
            postEvent(new UnavailableServiceEvent(getName()));
            pause();
        }
        else {
            getLogger().error("Unknown StatusRuntimeException when executing call", e);
            System.exit(1);
        }
    }

    private UnaryServiceMethodInvoker<DynamicMessage, DynamicMessage> buildInvoker() {
        return UnaryServiceMethodInvoker.<DynamicMessage, DynamicMessage>newBuilder()
                .forChannel(getChannel())
                .forMethod(buildGrpcMethodDescriptor())
                .build();
    }
}