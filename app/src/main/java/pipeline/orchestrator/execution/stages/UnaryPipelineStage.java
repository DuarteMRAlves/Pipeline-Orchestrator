package pipeline.orchestrator.execution.stages;

import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import pipeline.core.invocation.UnaryServiceMethodInvoker;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.grpc.FullMethodDescription;

/**
 * Stage that executes an Unary Grpc Method
 */
public class UnaryPipelineStage extends AbstractPipelineStage {

    private UnaryServiceMethodInvoker<DynamicMessage, DynamicMessage> invoker;

    UnaryPipelineStage(
            StageInformation stageInformation,
            Channel channel,
            FullMethodDescription fullMethodDescription) {
        super(stageInformation,  channel, fullMethodDescription);

        invoker = buildInvoker();
        getLogger().info(
                "Connection to processing service at {}:{} with method {}",
                stageInformation.getServiceHost(),
                stageInformation.getServicePort(),
                getFullMethodDescription().getMethodFullName());
    }

    @Override
    public void run() {
        boolean running = true;

        StageInputStream inputStream = getStageInputStream();
        StageOutputStream outputStream = getStageOutputStream();
        while (running) {
            ComputationState requestState = inputStream.get();
            DynamicMessage response;
            try {
                response = invoker.call(requestState.getMessage());
            }
            catch (StatusRuntimeException e) {
                getLogger().warn("Unable to execute call", e);
                System.exit(1);
                return;
            }
            ComputationState responseState = ComputationState.from(
                    requestState,
                    response);
            outputStream.accept(responseState);

            if (Thread.currentThread().isInterrupted()) {
                running = false;
            }
        }
    }

    private UnaryServiceMethodInvoker<DynamicMessage, DynamicMessage> buildInvoker() {
        return UnaryServiceMethodInvoker.<DynamicMessage, DynamicMessage>newBuilder()
                .forChannel(getChannel())
                .forMethod(buildGrpcMethodDescriptor())
                .build();
    }
}