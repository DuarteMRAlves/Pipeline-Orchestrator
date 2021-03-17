package pipeline.orchestrator.reflection;

import com.google.protobuf.Descriptors.ServiceDescriptor;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.junit.Test;
import pipeline.orchestrator.common.TestWithBindableService;
import pipeline.orchestrator.grpc.methods.SubtractingServiceGrpc;
import pipeline.orchestrator.grpc.services.AddingServiceGrpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class FindServiceByNameTest extends TestWithBindableService {

    // Channel to be used in the tests
    // Set after the server creation
    private Channel channel;

    private static final ServiceFinder serviceFinder = new ServiceFinder();

    @Test
    public void testOneService() throws Exception {
        setUpServerImpl(
                new AddingServiceGrpc.AddingServiceImplBase() {},
                ProtoReflectionService.newInstance());

        ServiceDescriptor serviceDescriptor = serviceFinder.findServiceByName(
                channel,
                AddingServiceGrpc.SERVICE_NAME);

        assertEquals(AddingServiceGrpc.SERVICE_NAME, serviceDescriptor.getName());
    }

    @Test
    public void testMultipleServices() throws Exception {
        setUpServerImpl(
                new AddingServiceGrpc.AddingServiceImplBase() {},
                new SubtractingServiceGrpc.SubtractingServiceImplBase() {},
                ProtoReflectionService.newInstance()
        );

        ServiceDescriptor serviceDescriptor = serviceFinder.findServiceByName(
                channel,
                AddingServiceGrpc.SERVICE_NAME);

        assertEquals(AddingServiceGrpc.SERVICE_NAME, serviceDescriptor.getName());
    }

    @Test
    public void testNoService() throws Exception {
        setUpServerImpl(ProtoReflectionService.newInstance());

        UnableToDiscoverMethodException exception =
                assertThrows(
                        UnableToDiscoverMethodException.class,
                        () -> serviceFinder.findServiceByName(channel, AddingServiceGrpc.SERVICE_NAME));

        assertEquals(
                "Unable to discover method: Unable to lookup service at localhost",
                exception.getMessage());
    }

    @Test
    public void testNoSuchService() throws Exception {
        setUpServerImpl(
                new SubtractingServiceGrpc.SubtractingServiceImplBase() {},
                ProtoReflectionService.newInstance());

        UnableToDiscoverMethodException exception =
                assertThrows(
                        UnableToDiscoverMethodException.class,
                        () -> serviceFinder.findServiceByName(channel, AddingServiceGrpc.SERVICE_NAME));

        assertEquals(
                "Unable to discover method: Unable to lookup service at localhost",
                exception.getMessage());
    }

    @Test
    public void testNoReflection() throws Exception {
        setUpServerImpl(
                new AddingServiceGrpc.AddingServiceImplBase() {}
        );

        UnableToDiscoverMethodException exception =
                assertThrows(
                        UnableToDiscoverMethodException.class,
                        () -> serviceFinder.findServiceByName(channel, AddingServiceGrpc.SERVICE_NAME));

        assertEquals(
                "Unable to discover method: Unable to lookup service at localhost",
                exception.getMessage());
    }

    @Override
    protected void setUpAfterServerImpl(ManagedChannel channel) {
        this.channel = channel;
    }
}
