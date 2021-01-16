package pipeline.orchestrator.grpc.reflection;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc.ServerReflectionImplBase;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import pipeline.orchestrator.common.TestWithBindableService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class ReflectionStreamManagerTest extends TestWithBindableService {

    private static final String REFLECTION_HOST_1 = "reflection host 1";
    private static final String REFLECTION_HOST_2 = "reflection host 2";

    private static final ServerReflectionRequest REFLECTION_REQUEST_1
            = ServerReflectionRequest.newBuilder().setHost("localhost1").setListServices("").build();
    private static final ServerReflectionRequest REFLECTION_REQUEST_2
            = ServerReflectionRequest.newBuilder().setHost("localhost2").setListServices("").build();
    private static final ServerReflectionResponse REFLECTION_RESPONSE_1
            = ServerReflectionResponse.newBuilder().setValidHost(REFLECTION_HOST_1).build();
    private static final ServerReflectionResponse REFLECTION_RESPONSE_2
            = ServerReflectionResponse.newBuilder().setValidHost(REFLECTION_HOST_2).build();

    private ReflectionStreamManager streamManager;

    /* Correct tests */

    @Test
    public void multipleRequests() throws Exception {
        // Given
        final List<ServerReflectionRequest> requestsDelivered = new ArrayList<>();
        final boolean[] errors = {false};
        final boolean[] completed = {false};
        setUpServerImpl(new ServerReflectionImplBase() {
            @Override
            public StreamObserver<ServerReflectionRequest> serverReflectionInfo(StreamObserver<ServerReflectionResponse> responseObserver) {
                return new StreamObserver<>() {
                    private boolean first = true;
                    @Override
                    public void onNext(ServerReflectionRequest value) {
                        requestsDelivered.add(value);
                        if (first) {
                            first = false;
                            responseObserver.onNext(REFLECTION_RESPONSE_1);
                        }
                        else {
                            responseObserver.onNext(REFLECTION_RESPONSE_2);
                        }
                    }

                    @Override
                    public void onError(Throwable t) { errors[0] = true; }

                    @Override
                    public void onCompleted() { completed[0] = true; responseObserver.onCompleted(); }
                };
            }
        });

        // When
        Future<ServerReflectionResponse> responseFuture1 = streamManager.submit(REFLECTION_REQUEST_1);
        Future<ServerReflectionResponse> responseFuture2 = streamManager.submit(REFLECTION_REQUEST_2);
        streamManager.complete();

        // Then
        assertEquals(REFLECTION_RESPONSE_1, responseFuture1.get());
        assertTrue(responseFuture1.isDone());
        assertEquals(REFLECTION_RESPONSE_2, responseFuture2.get());
        assertTrue(responseFuture2.isDone());
        assertEquals(Arrays.asList(REFLECTION_REQUEST_1, REFLECTION_REQUEST_2), requestsDelivered);
        assertTrue(completed[0]);
        assertFalse(errors[0]);
    }

    @Test
    public void multipleRequestsInverseOrder() throws Exception {
        // Given
        final List<ServerReflectionRequest> requestsDelivered = new ArrayList<>();
        setUpServerImpl(new ServerReflectionImplBase() {
            @Override
            public StreamObserver<ServerReflectionRequest> serverReflectionInfo(StreamObserver<ServerReflectionResponse> responseObserver) {
                return new StreamObserver<>() {
                    private boolean first = true;
                    @Override
                    public void onNext(ServerReflectionRequest value) {
                        requestsDelivered.add(value);
                        if (first) {
                            first = false;
                            responseObserver.onNext(REFLECTION_RESPONSE_1);
                        }
                        else {
                            responseObserver.onNext(REFLECTION_RESPONSE_2);
                        }
                    }

                    @Override
                    public void onError(Throwable t) { /* Do nothing */ }

                    @Override
                    public void onCompleted() { responseObserver.onCompleted(); }
                };
            }
        });

        // When
        Future<ServerReflectionResponse> responseFuture1 = streamManager.submit(REFLECTION_REQUEST_1);
        Future<ServerReflectionResponse> responseFuture2 = streamManager.submit(REFLECTION_REQUEST_2);
        streamManager.complete();

        // Then
        assertEquals(REFLECTION_RESPONSE_2, responseFuture2.get());
        assertTrue(responseFuture2.isDone());
        assertEquals(REFLECTION_RESPONSE_1, responseFuture1.get());
        assertTrue(responseFuture1.isDone());
        assertEquals(Arrays.asList(REFLECTION_REQUEST_1, REFLECTION_REQUEST_2), requestsDelivered);
    }

    @Test
    public void onError() throws Exception {
        // Given
        setUpServerImpl(new ServerReflectionImplBase() {
            @Override
            public StreamObserver<ServerReflectionRequest> serverReflectionInfo(StreamObserver<ServerReflectionResponse> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(ServerReflectionRequest value) {
                        responseObserver.onError(new RuntimeException("Dummy Exception"));
                    }

                    @Override
                    public void onError(Throwable t) { /* Do nothing */ }

                    @Override
                    public void onCompleted() { /* Do nothing */ }
                };
            }
        });
        // When
        Future<ServerReflectionResponse> responseFuture = streamManager.submit(REFLECTION_REQUEST_1);
        // Then
        // ExecutionException because access to result of task aborted by exception
        ExecutionException exception = assertThrows(ExecutionException.class, responseFuture::get);
        assertEquals(StatusRuntimeException.class, exception.getCause().getClass());
        assertTrue(responseFuture.isDone());
    }

    @Test
    public void unexpectedCompletion() throws Exception {
        // Given
        setUpServerImpl(new ServerReflectionImplBase() {
            @Override
            public StreamObserver<ServerReflectionRequest> serverReflectionInfo(StreamObserver<ServerReflectionResponse> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(ServerReflectionRequest value) {
                        responseObserver.onCompleted(); // Complete without response
                    }

                    @Override
                    public void onError(Throwable t) { /* Do nothing */ }

                    @Override
                    public void onCompleted() { /* Do nothing */ }
                };
            }
        });
        // When
        Future<ServerReflectionResponse> responseFuture = streamManager.submit(REFLECTION_REQUEST_1);

        // Then
        // ExecutionException because access to result of task aborted by exception
        ExecutionException exception = assertThrows(ExecutionException.class, responseFuture::get);
        assertEquals(ReflectionStreamUnexpectedCompletionException.class, exception.getCause().getClass());
        assertTrue(responseFuture.isDone());
    }

    @Override
    protected void setUpAfterServerImpl(ManagedChannel channel) {
        streamManager = ReflectionStreamManager.newBuilder()
                .forChannel(channel)
                .forMaxSimultaneousRequests(10)
                .build();
    }
}

