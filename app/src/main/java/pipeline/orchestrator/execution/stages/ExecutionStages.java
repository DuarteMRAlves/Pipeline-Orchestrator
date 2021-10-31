package pipeline.orchestrator.execution.stages;

import com.google.protobuf.Descriptors;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.Link;
import pipeline.orchestrator.grpc.methods.FullMethodDescription;
import pipeline.orchestrator.reflection.MethodSearchInformation;
import pipeline.orchestrator.reflection.ServerMethodDiscovery;
import pipeline.orchestrator.reflection.UnableToDiscoverMethodException;

import java.util.Optional;

/**
 * Class to assist the usage of PipelineStages
 * like creation and binding of inputs
 * Acts as an API for creating and operating with PipelineStages
 */
public class ExecutionStages {

    private static final Logger LOGGER = LogManager.getLogger(ExecutionStages.class);

    private ExecutionStages() {}

    /**
     * Builds a new stage that is linked to the given stage
     * @param stageInformation information of the stage that this pipeline
     *                         stage will link to.
     * @param listener listener for the stage to call.
     * @return the pipeline stage that can be executed to execute
     *         requests on the given stage
     */
    public static ExecutionStage buildStage(
            StageInformation stageInformation,
            StageListener listener
    ) {
        logBuildStage(stageInformation);
        return buildStageFromInformation(stageInformation, listener);
    }

    /**
     * Method to link 2 pipeline stages
     * @param source stage that produces the messages
     * @param target stage that receives the messages
     * @param linkInformation information about the messages sent
     */
    public static void linkStages(
            ExecutionStage source,
            ExecutionStage target,
            LinkInformation linkInformation) {

        logLinkStages(source, target, linkInformation);

        String sourceFieldName = linkInformation.getSourceFieldName().orElse("");
        String targetFieldName = linkInformation.getTargetFieldName().orElse("");
        Link link = new Link();
        source.bindOutput(sourceFieldName, link);
        target.bindInput(targetFieldName, link);
    }

    private static ExecutionStage buildStageFromInformation(
            StageInformation stageInformation,
            StageListener listener
    ) {

        Channel channel = ManagedChannelBuilder
                .forAddress(
                        stageInformation.getServiceHost(),
                        stageInformation.getServicePort())
                .usePlaintext().build();

        Optional<FullMethodDescription> fullMethodDesc =
                getFullMethodDescription(channel, stageInformation);

        return fullMethodDesc.map(desc -> getPipelineStage(stageInformation, channel, desc, listener))
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Unable to build pipeline stage for %s", channel.authority())));
    }

    private static Optional<FullMethodDescription> getFullMethodDescription(
            Channel channel,
            StageInformation stageInformation
    ) {
        MethodSearchInformation.Builder builder =
                MethodSearchInformation.newBuilder();

        stageInformation.getServiceName().ifPresent(builder::setServiceName);
        stageInformation.getMethodName().ifPresent(builder::setMethodName);

        try {
            return Optional.of(ServerMethodDiscovery.discoverMethod(
                    channel,
                    builder.build()));
        } catch (UnableToDiscoverMethodException exception) {
            LOGGER.warn("Unable get method description for {}",
                        channel.authority(),
                        exception);
            return Optional.empty();
        }
    }

    private static ExecutionStage getPipelineStage(
            StageInformation stageInformation,
            Channel channel,
            FullMethodDescription fullMethodDesc,
            StageListener listener) {

        // Builder to use when building the new stage
        ExecutionStageBuilder<?> builder = getStageBuilder(
                stageInformation,
                fullMethodDesc.getMethodDescriptor());

        return builder
                .setName(stageInformation.getName())
                .setChannel(channel)
                .setFullMethodDescription(fullMethodDesc)
                .setListener(listener)
                .build();
    }

    private static ExecutionStageBuilder<?> getStageBuilder(
            StageInformation stageInformation,
            Descriptors.MethodDescriptor methodDescriptor) {

        ExecutionStageBuilder<?> builder;
        if (isUnary(methodDescriptor) && isOneShot(stageInformation)) {
            builder = OneShotUnaryStage.newBuilder();
        }
        else if (isUnary(methodDescriptor) && !isOneShot(stageInformation)) {
            builder = UnaryStage.newBuilder();
        }
        else if (isServerStreaming(methodDescriptor) && !isOneShot(stageInformation)) {
            builder = ServerStreamingStage.newBuilder();
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
        return stageInformation.isOneShot();
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
            ExecutionStage source,
            ExecutionStage target,
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
