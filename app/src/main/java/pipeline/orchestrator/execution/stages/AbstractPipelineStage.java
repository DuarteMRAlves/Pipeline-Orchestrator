package pipeline.orchestrator.execution.stages;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.core.invocation.DynamicMessages;
import pipeline.core.invocation.MethodDescriptors;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.Link;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.grpc.FailedToExecuteRequestException;
import pipeline.orchestrator.grpc.FullMethodDescription;
import pipeline.orchestrator.grpc.ServerMethodDiscovery;

import java.util.Optional;

/**
 * Class for an abstract pipeline stage
 * Offers useful methods that stages can use to run
 */
public abstract class AbstractPipelineStage implements Runnable {

    private final Logger logger;

    // Multimaps with this input and output links for the stage
    private final Multimap<String, Link> inputs = HashMultimap.create();
    private final Multimap<String, Link> outputs = HashMultimap.create();

    private final Channel channel;

    private FullMethodDescription fullMethodDescription;

    // Variable to check if a method that implies that the stage is
    // running was called and so any configuration commands should fail
    private boolean setupComplete = false;

    protected AbstractPipelineStage(
            StageInformation stageInformation,
            Channel channel,
            FullMethodDescription fullMethodDescription) {

        logger = LogManager.getLogger(
                String.format(
                        "%s - %s",
                        AbstractPipelineStage.class.getName(),
                        stageInformation.getName()));

        this.channel = channel;
        this.fullMethodDescription = fullMethodDescription;
    }

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
        return DynamicMessages.newMethodDescriptor(
                fullMethodDescription.getMethodFullName(),
                MethodDescriptors.getType(fullMethodDescription.getMethodDescriptor()),
                fullMethodDescription.getMethodDescriptor().getInputType(),
                fullMethodDescription.getMethodDescriptor().getOutputType());
    }

    protected StageInputStream getStageInputStream() {
        setupComplete = true;
        return StageInputStream.forInputs(
                fullMethodDescription.getMethodDescriptor().getInputType(),
                ImmutableSetMultimap.copyOf(inputs));
    }

    protected StageOutputStream getStageOutputStream() {
        setupComplete = true;
        return StageOutputStream.forOutputs(
                fullMethodDescription.getMethodDescriptor().getOutputType(),
                ImmutableSetMultimap.copyOf(outputs));
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return "AbstractPipelineStage{" +
                "target=" + channel.authority() +
                "method=" + fullMethodDescription.getMethodFullName() +
                '}';
    }
}