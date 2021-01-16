package pipeline.orchestrator.grpc.methods;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import pipeline.orchestrator.common.TestWithBindableService;
import pipeline.orchestrator.grpc.messages.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsyncServerStreamingMethodInvokerTest extends TestWithBindableService {

    private static final Data REQUEST_1 = Data.newBuilder()
            .setNum(1)
            .build();

    private static final Data REQUEST_2 = Data.newBuilder()
            .setNum(2)
            .build();

    private static final Data REPLY_1 = Data.newBuilder()
            .setNum(10)
            .build();


    private static final Data REPLY_2 = Data.newBuilder()
            .setNum(20)
            .build();

    private static final Data REPLY_3 = Data.newBuilder()
            .setNum(30)
            .build();


    private static final Data REPLY_4 = Data.newBuilder()
            .setNum(40)
            .build();

    private static final StatusRuntimeException EXCEPTION =
            new StatusRuntimeException(Status.ABORTED);

    private AsyncServerStreamingMethodInvoker<Data, Data> invoker;

    @Test
    public void testSingleCall() throws Exception {
        final List<Data> requestsDelivered = new ArrayList<>();
        final List<Data> responsesReceived = new ArrayList<>();
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        setUpServerImpl(new AddingServiceGrpc.AddingServiceImplBase() {
            @Override
            public void addServerStreaming(
                    Data request, StreamObserver<Data> responseObserver) {
                requestsDelivered.add(request);

                responseObserver.onNext(REPLY_1);
                responseObserver.onNext(REPLY_2);
                responseObserver.onCompleted();
            }
        });

        invoker.call(REQUEST_1, new StreamObserver<>() {
            @Override
            public void onNext(Data value) {
                responsesReceived.add(value);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();

        assertEquals(1, requestsDelivered.size());
        assertEquals(REQUEST_1, requestsDelivered.get(0));

        assertEquals(2, responsesReceived.size());
        assertEquals(REPLY_1, responsesReceived.get(0));
        assertEquals(REPLY_2, responsesReceived.get(1));
    }

    @Test
    public void testMultipleCalls() throws Exception {
        final List<Data> requestsDelivered = new ArrayList<>();
        final List<Data> responsesReceived1 = new ArrayList<>();
        final List<Data> responsesReceived2 = new ArrayList<>();

        final CountDownLatch countDownLatch1 = new CountDownLatch(1);
        final CountDownLatch countDownLatch2 = new CountDownLatch(1);

        setUpServerImpl(new AddingServiceGrpc.AddingServiceImplBase() {
            @Override
            public void addServerStreaming(
                    Data request, StreamObserver<Data> responseObserver) {
                requestsDelivered.add(request);

                if (request.equals(REQUEST_1)) {
                    responseObserver.onNext(REPLY_1);
                    responseObserver.onNext(REPLY_2);
                }
                else {
                    responseObserver.onNext(REPLY_3);
                    responseObserver.onNext(REPLY_4);
                }
                responseObserver.onCompleted();
            }
        });

        invoker.call(REQUEST_1, new StreamObserver<>() {
            @Override
            public void onNext(Data value) {
                responsesReceived1.add(value);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
                countDownLatch1.countDown();
            }
        });

        invoker.call(REQUEST_2, new StreamObserver<>() {
            @Override
            public void onNext(Data value) {
                responsesReceived2.add(value);
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onCompleted() {
                countDownLatch2.countDown();
            }
        });

        countDownLatch1.await();
        countDownLatch2.await();

        // Assert received requests
        assertEquals(2, requestsDelivered.size());
        assertTrue(requestsDelivered.contains(REQUEST_1));
        assertTrue(requestsDelivered.contains(REQUEST_2));

        // Assert Response 1
        assertEquals(2, responsesReceived1.size());
        assertEquals(REPLY_1, responsesReceived1.get(0));
        assertEquals(REPLY_2, responsesReceived1.get(1));

        // Assert Response 2
        assertEquals(2, responsesReceived2.size());
        assertEquals(REPLY_3, responsesReceived2.get(0));
        assertEquals(REPLY_4, responsesReceived2.get(1));
    }

    @Test
    public void testOnError() throws Exception {
        final List<Data> requestsDelivered = new ArrayList<>();
        final List<Data> responsesReceived = new ArrayList<>();
        final List<Throwable> errorsReceived = new ArrayList<>();
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        setUpServerImpl(new AddingServiceGrpc.AddingServiceImplBase() {
            @Override
            public void addServerStreaming(
                    Data request, StreamObserver<Data> responseObserver) {
                requestsDelivered.add(request);

                responseObserver.onError(EXCEPTION);
            }
        });

        invoker.call(REQUEST_1, new StreamObserver<>() {
            @Override
            public void onNext(Data value) {
                responsesReceived.add(value);
            }

            @Override
            public void onError(Throwable t) {
                errorsReceived.add(t);
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();

        assertEquals(1, requestsDelivered.size());
        assertEquals(REQUEST_1, requestsDelivered.get(0));

        assertEquals(0, responsesReceived.size());
        assertEquals(1, errorsReceived.size());
        assertEquals(EXCEPTION.getClass(), errorsReceived.get(0).getClass());
        assertEquals(EXCEPTION.getCause(), errorsReceived.get(0).getCause());
        assertEquals(
                EXCEPTION.getStatus(),
                ((StatusRuntimeException) errorsReceived.get(0)).getStatus());
    }

    @Override
    protected void setUpAfterServerImpl(ManagedChannel channel) {
        invoker = AsyncServerStreamingMethodInvoker.<Data,Data>newBuilder()
                .forChannel(channel)
                .forMethod(AddingServiceGrpc.getAddServerStreamingMethod())
                .build();
    }
}
