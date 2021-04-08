package pipeline.orchestrator.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import pipeline.orchestrator.grpc.messages.Data;
import pipeline.orchestrator.grpc.services.SinkServiceGrpc;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SinkService extends SinkServiceGrpc.SinkServiceImplBase {

    private final int requestLimit;
    private final List<Data> received;
    private final CountDownLatch countDownLatch;
    private int requestCount = 0;

    public SinkService(
            int requestLimit,
            List<Data> received,
            CountDownLatch countDownLatch
    ) {
        this.requestLimit = requestLimit;
        this.received = received;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void collect(Data request, StreamObserver<Empty> responseObserver) {
        requestCount++;
        if (requestCount < requestLimit) {
            received.add(request);
        } else if (requestCount == requestLimit) {
            received.add(request);
            countDownLatch.countDown();
        }
        // Always return default instance
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
