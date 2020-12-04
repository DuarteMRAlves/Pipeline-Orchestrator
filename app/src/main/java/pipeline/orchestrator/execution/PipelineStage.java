package pipeline.orchestrator.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.core.nodes.services.invocation.DynamicMessages;
import pipeline.core.nodes.services.invocation.MethodDescriptors;
import pipeline.core.nodes.services.invocation.UnaryServiceMethodInvoker;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.grpc.FailedToExecuteRequestException;
import pipeline.orchestrator.grpc.FullMethodDescription;
import pipeline.orchestrator.grpc.ServerMethodDiscovery;

public class PipelineStage implements Runnable {

    private final Logger logger;

    private boolean running = false;

    private FullMethodDescription methodDescription;

    private final Channel channel;
    private MethodDescriptor.Marshaller<DynamicMessage> inputMarshaller;
    private UnaryServiceMethodInvoker<DynamicMessage, DynamicMessage> invoker;

    private Multimap<String, Link> inputs = HashMultimap.create();
    private Multimap<String, Link> outputs = HashMultimap.create();

    public PipelineStage(StageInformation stageInformation) {
        String host = stageInformation.getServiceHost();
        int port = stageInformation.getServicePort();
        String name = stageInformation.getMethodName();

        if (name != null) {
            logger = LogManager.getLogger(
                    String.format("%s - %s:%d:%s", PipelineStage.class.getName(), host, port, name));
        }
        else {
            logger = LogManager.getLogger(
                    String.format("%s - %s:%d", PipelineStage.class.getName(), host, port));
        }

        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        try {
            if (name != null)
                methodDescription = ServerMethodDiscovery.discoverSingleMethod(channel, name);
            else
                methodDescription = ServerMethodDiscovery.discoverSingleMethod(channel);

            MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor =
                    buildGrpcMethodDescriptor();

            inputMarshaller = methodDescriptor.getRequestMarshaller();
            invoker = buildInvoker(methodDescriptor);
            logger.info(
                    "Connection to processing service at {}:{} with method {}",
                    host,
                    port,
                    methodDescription.getMethodFullName());
        } catch (FailedToExecuteRequestException exception) {
            logger.warn("Unable to connect to service", exception);
            System.exit(1);
        }
    }

    @Override
    public void run() {
        running = true;
        StageInputStream inputStream = StageInputStream.forInputs(
                methodDescription.getMethodDescriptor().getInputType(),
                ImmutableSetMultimap.copyOf(inputs));
        StageOutputStream outputStream = StageOutputStream.forOutputs(
                methodDescription.getMethodDescriptor().getOutputType(),
                ImmutableSetMultimap.copyOf(outputs));
        while (running) {
            DynamicMessage request = inputStream.get();
            DynamicMessage response;
            try {
                response = invoker.call(request);
            }
            catch (StatusRuntimeException e) {
                logger.warn("Unable to execute call", e);
                System.exit(1);
                return;
            }
            outputStream.accept(response);

            if (Thread.currentThread().isInterrupted()) {
                running = false;
            }
        }
    }

    public void shutdown() {
        running = false;
    }

    /**
     * Method to link 2 pipeline stages
     * @param source stage that produces the messages
     * @param target stage that receives the messages
     * @param linkInformation information about the messages sent
     */
    public static void linkStages(
            PipelineStage source,
            PipelineStage target,
            LinkInformation linkInformation) {

        Preconditions.checkState(!source.running && !target.running);

        String sourceFieldName = linkInformation.getSourceFieldName();
        String targetFieldName = linkInformation.getTargetFieldName();
        Link link = new Link(sourceFieldName, targetFieldName);
        source.outputs.put(sourceFieldName, link);
        target.inputs.put(targetFieldName, link);
    }

    @Override
    public String toString() {
        return "PipelineState {"
                + "target: " + channel.authority()
                + ", inputSize: " + inputs.size()
                + ", outputSize: " + outputs.size()
                + "}";
    }

    private MethodDescriptor<DynamicMessage, DynamicMessage> buildGrpcMethodDescriptor() {
        return DynamicMessages.newMethodDescriptor(
                methodDescription.getMethodFullName(),
                MethodDescriptors.getType(methodDescription.getMethodDescriptor()),
                methodDescription.getMethodDescriptor().getInputType(),
                methodDescription.getMethodDescriptor().getOutputType());
    }

    private UnaryServiceMethodInvoker<DynamicMessage, DynamicMessage> buildInvoker(
            MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor) {
        return UnaryServiceMethodInvoker.<DynamicMessage, DynamicMessage>newBuilder()
                .forChannel(channel)
                .forMethod(methodDescriptor)
                .build();
    }
}
