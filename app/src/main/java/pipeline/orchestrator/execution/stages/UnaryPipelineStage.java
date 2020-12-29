package pipeline.orchestrator.execution.stages;

import com.google.common.eventbus.EventBus;
import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import pipeline.core.common.grpc.StatusRuntimeExceptions;
import pipeline.core.invocation.UnaryServiceMethodInvoker;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.execution.stages.events.UnavailableServiceEvent;
import pipeline.orchestrator.grpc.FullMethodDescription;

/**
 * Stage that executes an Unary Grpc Method
 */
public class UnaryPipelineStage extends AbstractPipelineStage {

    private final UnaryServiceMethodInvoker<DynamicMessage, DynamicMessage> invoker;

    private boolean running = true;

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

        while (isRunning()) {
            ComputationState requestState = inputStream.get();
            try {
                DynamicMessage response = invoker.call(requestState.getMessage());
                ComputationState responseState = ComputationState.from(
                        requestState,
                        response);
                outputStream.accept(responseState);
            }
            catch (StatusRuntimeException e) {
                handleStatusRuntimeException(e);
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

    private void handleStatusRuntimeException(StatusRuntimeException e) {
        if (StatusRuntimeExceptions.isUnavailable(e)) {
            postEvent(new UnavailableServiceEvent(getName()));
            pauseStage();
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