package pipeline.orchestrator.execution.stages;

import com.google.protobuf.DynamicMessage;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pipeline.core.invocation.AsyncServerStreamingMethodInvoker;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.inputs.StageInputStream;
import pipeline.orchestrator.execution.outputs.StageOutputStream;
import pipeline.orchestrator.grpc.FullMethodDescription;

import java.util.concurrent.CountDownLatch;

/**
 * Stage that executes a Server Streaming Grpc Method
 */
public class ServerStreamingPipelineStage extends AbstractPipelineStage {

    private final AsyncServerStreamingMethodInvoker<DynamicMessage, DynamicMessage> invoker;

    ServerStreamingPipelineStage(
            StageInformation stageInformation,
            Channel channel,
            FullMethodDescription fullMethodDescription) {
        super(stageInformation,  channel, fullMethodDescription);

        invoker = AsyncServerStreamingMethodInvoker.<DynamicMessage, DynamicMessage>newBuilder()
                .forChannel(getChannel())
                .forMethod(buildGrpcMethodDescriptor())
                .build();

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
            DynamicMessage request = inputStream.get();
            CountDownLatch streamEnd = new CountDownLatch(1);
            try {
                invoker.call(request, new StreamObserver<>() {
                    @Override
                    public void onNext(DynamicMessage value) {
                        outputStream.accept(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        getLogger().warn("Unable to execute call", t);
                        System.exit(1);
                    }

                    @Override
                    public void onCompleted() {
                        streamEnd.countDown();
                    }
                });
            }
            catch (StatusRuntimeException e) {
                getLogger().warn("Unable to execute call", e);
                System.exit(1);
                return;
            }
            try {
                // Wait for the stream to end before starting the next stream
                streamEnd.await();
            } catch (InterruptedException e) {
                getLogger().warn("Interrupted", e);
                Thread.currentThread().interrupt();
            }

            if (Thread.currentThread().isInterrupted()) {
                running = false;
            }
        }
    }


}
