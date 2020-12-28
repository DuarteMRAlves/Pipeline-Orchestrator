package pipeline.orchestrator.execution.stages;

import com.google.common.eventbus.EventBus;
import com.google.protobuf.Descriptors;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.Link;
import pipeline.orchestrator.grpc.FailedToExecuteRequestException;
import pipeline.orchestrator.grpc.FullMethodDescription;
import pipeline.orchestrator.grpc.ServerMethodDiscovery;

import java.util.Optional;

/**
 * Class to assist the usage of PipelineStages
 * like creation and binding of inputs
 * Acts as an API for creating and operating with PipelineStages
 */
public class PipelineStages {

    private static final Logger LOGGER = LogManager.getLogger(PipelineStages.class);

    // Event bus for the stages to publish their error events
    private static final EventBus EVENT_BUS = new EventBus("StagesEventBus");

    private PipelineStages() {}

    /**
     * Builds a new stage that is linked to the given stage
     * @param stageInformation information of the stage that this pipeline
     *                         stage will linked too
     * @return the pipeline stage that can be executed to execute
     *         requests on the given stage
     */
    public static AbstractPipelineStage buildStage(StageInformation stageInformation) {
        return buildStageFromInformation(stageInformation);
    }

    /**
     * Method to link 2 pipeline stages
     * @param source stage that produces the messages
     * @param target stage that receives the messages
     * @param linkInformation information about the messages sent
     */
    public static void linkStages(
            AbstractPipelineStage source,
            AbstractPipelineStage target,
            LinkInformation linkInformation) {

        String sourceFieldName = linkInformation.getSourceFieldName().orElse("");
        String targetFieldName = linkInformation.getTargetFieldName().orElse("");
        Link link = new Link();
        source.bindOutput(sourceFieldName, link);
        target.bindInput(targetFieldName, link);
    }

    /**
     * Method to register a subscriber to receive error events from
     * all the stages
     * @param subscriber subscriber to register
     */
    public static void subscribeToStagesEvents(Object subscriber) {
        EVENT_BUS.register(subscriber);
    }

    private static AbstractPipelineStage buildStageFromInformation(StageInformation stageInformation) {

        Channel channel = ManagedChannelBuilder
                .forAddress(
                        stageInformation.getServiceHost(),
                        stageInformation.getServicePort())
                .usePlaintext().build();

        Optional<FullMethodDescription> fullMethodDesc = stageInformation.getMethodName()
                // if method name is present then get method description with name
                .flatMap(name -> getFullMethodDescription(channel, name))
                // else get without name
                .or(() -> getFullMethodDescription(channel));

        return fullMethodDesc.map(desc -> getPipelineStage(stageInformation, channel, desc))
                // Should never happen
                .orElseThrow(() -> new IllegalStateException("Unknwon Pipeline Stage"));
    }


    private static Optional<FullMethodDescription> getFullMethodDescription(
            Channel channel,
            String methodName) {

        try {
            return Optional.of(ServerMethodDiscovery.discoverSingleMethod(channel, methodName));
        } catch (FailedToExecuteRequestException exception) {
            LOGGER.warn("Unable to connect to service", exception);
            return Optional.empty();
        }
    }

    private static Optional<FullMethodDescription> getFullMethodDescription(
            Channel channel) {

        try {
            return Optional.of(ServerMethodDiscovery.discoverSingleMethod(channel));
        } catch (FailedToExecuteRequestException exception) {
            LOGGER.warn("Unable to connect to service", exception);
            return Optional.empty();
        }
    }

    private static AbstractPipelineStage getPipelineStage(
            StageInformation stageInformation,
            Channel channel,
            FullMethodDescription fullMethodDesc) {
        Descriptors.MethodDescriptor methodDescriptor = fullMethodDesc.getMethodDescriptor();
        if (isUnary(methodDescriptor)) {
            return new UnaryPipelineStage(
                    stageInformation.getName(),
                    channel,
                    fullMethodDesc,
                    EVENT_BUS);
        }
        else if (isServerStreaming(methodDescriptor)) {
            return new ServerStreamingPipelineStage(
                    stageInformation.getName(),
                    channel,
                    fullMethodDesc,
                    EVENT_BUS);
        }

        throw new UnsupportedOperationException("Unsupported method type");
    }

    private static boolean isUnary(Descriptors.MethodDescriptor descriptor) {
        return !descriptor.isServerStreaming() && !descriptor.isClientStreaming();
    }

    private static boolean isServerStreaming(Descriptors.MethodDescriptor descriptor) {
        return descriptor.isServerStreaming() && !descriptor.isClientStreaming();
    }
}
