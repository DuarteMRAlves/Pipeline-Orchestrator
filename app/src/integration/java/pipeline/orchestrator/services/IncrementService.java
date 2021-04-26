package pipeline.orchestrator.services;

import io.grpc.stub.StreamObserver;
import pipeline.orchestrator.base.RequestCounter;
import pipeline.orchestrator.grpc.messages.Data;
import pipeline.orchestrator.grpc.services.IncrementServiceGrpc;

public class IncrementService
        extends IncrementServiceGrpc.IncrementServiceImplBase {

    private final RequestCounter<Data> requestCounter;

    public IncrementService(RequestCounter<Data> requestCounter) {
        this.requestCounter = requestCounter;
    }

    @Override
    public void increment(
            Data request, StreamObserver<Data> responseObserver
    ) {
        requestCounter.processRequest(request);
        responseObserver.onNext(Data.newBuilder()
                                        .setNum(request.getNum() + 1)
                                        .build());
        responseObserver.onCompleted();
    }
}
