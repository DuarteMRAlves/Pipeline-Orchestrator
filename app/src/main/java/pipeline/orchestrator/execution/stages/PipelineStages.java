package pipeline.orchestrator.execution.stages;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.protobuf.Descriptors;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.Link;
import pipeline.orchestrator.reflection.UnableToDiscoverMethodException;
import pipeline.orchestrator.grpc.methods.FullMethodDescription;
import pipeline.orchestrator.reflection.ServerMethodDiscovery;

import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * Class to assist the usage of PipelineStages
 * like creation and binding of inputs
 * Acts as an API for creating and operating with PipelineStages
 */
public class PipelineStages {

    private static final Logger LOGGER = LogManager.getLogger(PipelineStages.class);

    // Event bus for the stages to publish their error events
    // Only single thread executor as not a lot of processing
    private static final EventBus EVENT_BUS = new AsyncEventBus(Executors.newSingleThreadExecutor());

    private PipelineStages() {}

    /**
     * Builds a new stage that is linked to the given stage
     * @param stageInformation information of the stage that this pipeline
     *                         stage will linked too
     * @return the pipeline stage that can be executed to execute
     *         requests on the given stage
     */
    public static AbstractPipelineStage buildStage(StageInformation stageInformation) {
        logBuildStage(stageInformation);
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

        logLinkStages(source, target, linkInformation);

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

        LOGGER.trace("New Pipeline Stages Events subscriber: {}", subscriber);

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
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Unable to build pipeline stage for %s", channel.authority())));
    }


    private static Optional<FullMethodDescription> getFullMethodDescription(
            Channel channel,
            String methodName) {

        try {
            return Optional.of(ServerMethodDiscovery.discoverSingleMethod(channel, methodName));
        } catch (UnableToDiscoverMethodException exception) {
            LOGGER.warn("Unable get method description for {}", channel.authority(), exception);
            return Optional.empty();
        }
    }

    private static Optional<FullMethodDescription> getFullMethodDescription(
            Channel channel) {

        try {
            return Optional.of(ServerMethodDiscovery.discoverSingleMethod(channel));
        } catch (UnableToDiscoverMethodException exception) {
            LOGGER.warn("Unable get method description for {}", channel.authority(), exception);
            return Optional.empty();
        }
    }

    private static AbstractPipelineStage getPipelineStage(
            StageInformation stageInformation,
            Channel channel,
            FullMethodDescription fullMethodDesc) {

        // Builder to use when building the new stage
        StageBuilder<?> builder = getStageBuilder(
                stageInformation,
                fullMethodDesc.getMethodDescriptor());

        return builder
                .setName(stageInformation.getName())
                .setChannel(channel)
                .setFullMethodDescription(fullMethodDesc)
                .setEventBus(EVENT_BUS)
                .build();
    }

    private static StageBuilder<?> getStageBuilder(
            StageInformation stageInformation,
            Descriptors.MethodDescriptor methodDescriptor) {

        StageBuilder<?> builder;
        if (isUnary(methodDescriptor) && isOneShot(stageInformation)) {
            builder = OneShotUnaryPipelineStage.newBuilder();
        }
        else if (isUnary(methodDescriptor) && !isOneShot(stageInformation)) {
            builder = UnaryPipelineStage.newBuilder();
        }
        else if (isServerStreaming(methodDescriptor) && !isOneShot(stageInformation)) {
            builder = ServerStreamingPipelineStage.newBuilder();
        }
        else {
            throw new UnsupportedOperationException("Unsupported method type");
        }
        return builder;
    }

    private static boolean isUnary(Descriptors.MethodDescriptor descriptor) {
        return !descriptor.isServerStreaming() && !descriptor.isClientStreaming();
    }

    private static boolean isServerStreaming(Descriptors.MethodDescriptor descriptor) {
        return descriptor.isServerStreaming() && !descriptor.isClientStreaming();
    }

    private static boolean isOneShot(StageInformation stageInformation) {
        return stageInformation.getOneShot();
    }

    private static void logBuildStage(StageInformation stageInformation) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "Stage '{}': Building stage from {}",
                    stageInformation.getName(),
                    stageInformation);
        }
        else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Stage '{}': Building stage",
                    stageInformation.getName());
        }
    }

    private static void logLinkStages(
            AbstractPipelineStage source,
            AbstractPipelineStage target,
            LinkInformation linkInformation) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(
                    "Linking stage {} to {} with information {}",
                    source,
                    target,
                    linkInformation);
        }
        else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Linking stage '{}' to '{}' ",
                    source.getName(),
                    target.getName());
        }
    }
}
