package pipeline.orchestrator.execution.stages;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.EventBus;
import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.execution.Link;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.grpc.methods.FullMethodDescription;
import pipeline.orchestrator.grpc.methods.MethodDescriptors;

/**
 * Class for an abstract pipeline stage
 * Offers useful methods that stages can use to run
 */
public abstract class ExecutionStage implements Runnable {

    private final Logger logger = LogManager.getLogger(ExecutionStage.class);

    // Multimaps with this input and output links for the stage
    private final Multimap<String, Link> inputs = HashMultimap.create();
    private final Multimap<String, Link> outputs = HashMultimap.create();

    private final String name;

    private final Channel channel;

    private final FullMethodDescription fullMethodDescription;

    private final EventBus eventBus;

    // Variable to check if a method that implies that the stage is
    // running was called and so any configuration commands should fail
    private boolean setupComplete = false;

    protected ExecutionStage(
            String stageName,
            Channel channel,
            FullMethodDescription fullMethodDescription,
            EventBus eventBus) {

        this.name = stageName;
        this.channel = channel;
        this.fullMethodDescription = fullMethodDescription;
        this.eventBus = eventBus;

        if (logger.isInfoEnabled()) {
            logger.info(
                    "Connection to processing service at {} with method {}",
                    channel.authority(),
                    getFullMethodDescription().getMethodFullName());
        }
    }

    /**
     * @return name of the stage
     */
    public final String getName() { return name; }

    /**
     * Indicates the the stage should resume its
     * processing after being paused by
     * {@link ExecutionStage#pause()}.
     */
    public abstract void resume();

    /**
     * Indicates that the stage should temporarily
     * stop processing messages. The stage can resume
     * processing messages with {@link ExecutionStage#resume()}
     */
    public abstract void pause();

    /**
     * Indicates that the stage should finish. The stage
     * will terminate and no longer will be able to run.
     * The run function should release all resources and the
     * stage should exit.
     * Subsequent calls to resume or pause should not do
     * anything since the stage is finished
     */
    public abstract void finish();

    /**
     * Binds the field to the inputs of the stage
     * @param fieldName the name of the field that should be used
     * @param link the link to receive the message from
     */
    void bindInput(String fieldName, Link link) {
        Preconditions.checkState(!setupComplete);
        inputs.put(fieldName, link);
    }

    /**
     * Binds the field to the outputs of the stage
     * @param fieldName the name of the field that should be used
     * @param link the link to send the message to
     */
    void bindOutput(String fieldName, Link link) {
        Preconditions.checkState(!setupComplete);
        outputs.put(fieldName, link);
    }

    protected Channel getChannel() {
        return channel;
    }

    protected FullMethodDescription getFullMethodDescription() {
        return fullMethodDescription;
    }

    protected MethodDescriptor<DynamicMessage, DynamicMessage> buildGrpcMethodDescriptor() {
        return MethodDescriptors.fromDescription(fullMethodDescription);
    }

    protected StageInputStream getStageInputStream() {
        logger.trace("Stage '{}': Building Input Stream", name);

        setupComplete = true;
        return StageInputStream.forInputs(
                fullMethodDescription.getMethodDescriptor().getInputType(),
                ImmutableSetMultimap.copyOf(inputs));
    }

    protected StageOutputStream getStageOutputStream() {
        logger.trace("Stage '{}': Building Output Stream", name);

        setupComplete = true;
        return StageOutputStream.forOutputs(
                fullMethodDescription.getMethodDescriptor().getOutputType(),
                ImmutableSetMultimap.copyOf(outputs));
    }

    protected Logger getLogger() {
        return logger;
    }

    /**
     * Method for a stage to post an event in the respective
     * event bus
     * @param object event to post
     */
    protected final void postEvent(Object object) {
        eventBus.post(object);
    }

    @Override
    public String toString() {
        return "PipelineStage{" +
                "name='" + name + '\'' +
                ", target='" + channel.authority() + '\'' +
                ", method='" + fullMethodDescription.getMethodFullName() + '\'' +
                '}';
    }
}
