package pipeline.orchestrator.services;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import pipeline.orchestrator.grpc.messages.Data;
import pipeline.orchestrator.grpc.services.SourceServiceGrpc;

public class SourceService extends SourceServiceGrpc.SourceServiceImplBase {

    private int counter = 0;

    @Override
    public void gen(Empty request, StreamObserver<Data> responseObserver) {
        System.out.println("Generating request");
        responseObserver.onNext(Data.newBuilder().setNum(counter++).build());
        responseObserver.onCompleted();
    }
}
