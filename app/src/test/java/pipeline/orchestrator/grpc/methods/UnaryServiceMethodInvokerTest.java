package pipeline.orchestrator.grpc.methods;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import pipeline.orchestrator.common.TestWithBindableService;
import pipeline.orchestrator.grpc.messages.Data;
import pipeline.orchestrator.grpc.services.AddingServiceGrpc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UnaryServiceMethodInvokerTest extends TestWithBindableService {

    private static final Data REQUEST_1 = Data.newBuilder()
            .setNum(1)
            .build();

    private static final Data REQUEST_2 = Data.newBuilder()
            .setNum(2)
            .build();

    private UnaryServiceMethodInvoker<Data, Data> invoker;

    @Test
    public void testSingleMessage() throws Exception {
        final List<Data> requestsDelivered = new ArrayList<>();
        setUpServerImpl(new AddingServiceGrpc.AddingServiceImplBase() {
            @Override
            public void add(Data request, StreamObserver<Data> responseObserver) {
                requestsDelivered.add(request);
                responseObserver.onNext(Data.newBuilder()
                        .setNum(request.getNum() + 1)
                        .build());
                responseObserver.onCompleted();
            }
        });

        Data response = invoker.call(REQUEST_1);

        assertEquals( 1, requestsDelivered.size());
        assertEquals(REQUEST_1, requestsDelivered.get(0));
        assertNotNull(response);
        assertEquals(2, response.getNum());
    }

    @Test
    public void testMultipleMessages() throws Exception {
        final List<Data> requestsDelivered = new ArrayList<>();
        setUpServerImpl(new AddingServiceGrpc.AddingServiceImplBase() {
            @Override
            public void add(Data request, StreamObserver<Data> responseObserver) {
                requestsDelivered.add(request);
                responseObserver.onNext(Data.newBuilder()
                        .setNum(request.getNum() + 1)
                        .build());
                responseObserver.onCompleted();
            }
        });

        Data response_1 = invoker.call(REQUEST_1);
        Data response_2 = invoker.call(REQUEST_2);

        assertEquals( 2, requestsDelivered.size());
        assertEquals(REQUEST_1, requestsDelivered.get(0));
        assertEquals(REQUEST_2, requestsDelivered.get(1));
        assertNotNull(response_1);
        assertNotNull(response_2);
        assertEquals(2, response_1.getNum());
        assertEquals(3, response_2.getNum());
    }

    @Test
    public void testOnError() throws Exception {
        final List<Data> requestsDelivered = new ArrayList<>();
        setUpServerImpl(new AddingServiceGrpc.AddingServiceImplBase() {
            @Override
            public void add(Data request, StreamObserver<Data> responseObserver) {
                requestsDelivered.add(request);
                responseObserver.onError(new StatusRuntimeException(Status.ABORTED));
            }
        });

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class,
                                                        () -> invoker.call(REQUEST_1));

        assertEquals( 1, requestsDelivered.size());
        assertEquals(Status.ABORTED, exception.getStatus());
    }

    @Override
    protected void setUpAfterServerImpl(ManagedChannel channel) {
        invoker = UnaryServiceMethodInvoker.<Data, Data>newBuilder()
                .forChannel(channel)
                .forMethod(AddingServiceGrpc.getAddMethod())
                .build();
    }
}
