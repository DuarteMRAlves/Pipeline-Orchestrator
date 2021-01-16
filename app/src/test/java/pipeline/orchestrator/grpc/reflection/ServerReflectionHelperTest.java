package pipeline.orchestrator.grpc.reflection;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import io.grpc.ManagedChannel;
import io.grpc.reflection.v1alpha.*;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc.ServerReflectionImplBase;
import io.grpc.stub.StreamObserver;
import org.junit.Test;
import pipeline.orchestrator.common.TestWithBindableService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ServerReflectionHelperTest extends TestWithBindableService {

    ServerReflectionHelper helper;

    private static final ListServiceResponse LIST_SERVICE_RESPONSE
            = ListServiceResponse.newBuilder()
            .addService(ServiceResponse.newBuilder().setName("Service 1").build())
            .addService(ServiceResponse.newBuilder().setName("Service 2").build())
            .addService(ServiceResponse.newBuilder().setName("Service 3").build())
            .build();

    private static final ServerReflectionResponse LIST_SERVICE_REFLECTION_RESPONSE
            = ServerReflectionResponse.newBuilder()
            .setListServicesResponse(LIST_SERVICE_RESPONSE)
            .build();

    private static final FileDescriptorResponse FILE_DESCRIPTOR_RESPONSE
            = FileDescriptorResponse.newBuilder()
            .addFileDescriptorProto(ByteString.EMPTY)
            .build();

    private static final ServerReflectionResponse LOOKUP_SERVICE_RESPONSE
            = ServerReflectionResponse.newBuilder()
            .setFileDescriptorResponse(FILE_DESCRIPTOR_RESPONSE)
            .build();

    @Test
    public void listServicesTest() throws Exception {
        // Given
        final List<ServerReflectionRequest> requestsDelivered = new ArrayList<>();
        final boolean[] errors = {false};
        final boolean[] completed = {false};
        setUpServerImpl(new ServerReflectionImplBase() {
            @Override
            public StreamObserver<ServerReflectionRequest> serverReflectionInfo(StreamObserver<ServerReflectionResponse> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(ServerReflectionRequest value) {
                        requestsDelivered.add(value);
                        responseObserver.onNext(LIST_SERVICE_REFLECTION_RESPONSE);
                    }

                    @Override
                    public void onError(Throwable t) { errors[0] = true; }

                    @Override
                    public void onCompleted() {
                        completed[0] = true;
                        responseObserver.onCompleted();
                    }
                };
            }
        });

        // When
        ImmutableSet<String> services = helper.listServices();

        // Then
        assertEquals(1, requestsDelivered.size());
        assertEquals(
                ServerReflectionRequest.newBuilder()
                        .setHost("localhost")
                        .setListServices("")
                        .build(),
                requestsDelivered.get(0));
        assertEquals(3, services.size());
        assertTrue(services.containsAll(
                Arrays.asList("Service 1", "Service 2", "Service 3")));
        assertTrue(completed[0]);
        assertFalse(errors[0]);
    }

    @Test
    public void listServicesErrorTest() throws Exception {
        final List<ServerReflectionRequest> requestsDelivered = new ArrayList<>();
        final boolean[] errors = {false};
        final boolean[] completed = {false};

        setUpServerImpl(new ServerReflectionImplBase() {
            @Override
            public StreamObserver<ServerReflectionRequest> serverReflectionInfo(StreamObserver<ServerReflectionResponse> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(ServerReflectionRequest value) {
                        requestsDelivered.add(value);
                        responseObserver.onError(new RuntimeException("Dummy Exception"));
                    }

                    @Override
                    public void onError(Throwable t) { errors[0] = true; }

                    @Override
                    public void onCompleted() {
                        completed[0] = true;
                        responseObserver.onCompleted();
                    }
                };
            }
        });

        // When
        assertThrows(UnableToListServicesException.class, () -> helper.listServices());

        // Then
        assertEquals(1, requestsDelivered.size());
        assertEquals(
                ServerReflectionRequest.newBuilder()
                        .setHost("localhost")
                        .setListServices("")
                        .build(),
                requestsDelivered.get(0));
        assertFalse(completed[0]);
        assertFalse(errors[0]);
    }

    @Test
    public void lookupServiceTest() throws Exception {
        // Given
        final List<ServerReflectionRequest> requestsDelivered = new ArrayList<>();
        final boolean[] errors = {false};
        final boolean[] completed = {false};
        setUpServerImpl(new ServerReflectionImplBase() {
            @Override
            public StreamObserver<ServerReflectionRequest> serverReflectionInfo(StreamObserver<ServerReflectionResponse> responseObserver) {
                return new StreamObserver<>() {
                    @Override
                    public void onNext(ServerReflectionRequest value) {
                        requestsDelivered.add(value);
                        responseObserver.onNext(LOOKUP_SERVICE_RESPONSE);
                    }

                    @Override
                    public void onError(Throwable t) { errors[0] = true; }

                    @Override
                    public void onCompleted() {
                        completed[0] = true;
                        responseObserver.onCompleted();
                    }
                };
            }
        });

        // When
        FileDescriptorSet fileDescriptorSet = helper.lookupService("service");
        // Then
        assertEquals(1, requestsDelivered.size());
        assertEquals(
                ServerReflectionRequest.newBuilder()
                        .setHost("localhost")
                        .setFileContainingSymbol("service")
                        .build(),
                requestsDelivered.get(0));
        assertEquals(1, fileDescriptorSet.getFileCount());
        assertTrue(completed[0]);
        assertFalse(errors[0]);
    }

    @Override
    protected void setUpAfterServerImpl(ManagedChannel channel) {
        helper = ServerReflectionHelper.newBuilder()
                .forChannel(channel)
                .build();
    }
}

