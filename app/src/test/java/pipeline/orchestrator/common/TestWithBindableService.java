package pipeline.orchestrator.common;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;

import java.io.IOException;

/**
 * Class to help with tests that have
 * grpc services by managing the server and
 * the channel to contact the server
 */
public abstract class TestWithBindableService {

    // This rule manages automatic graceful shutdown for the registered server at the end of test.
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    /**
     * Creates a new server for the test that
     * will be destroyed when the test ends.
     * Also creates a channel that will be destroyed
     * Calls the function {@link TestWithBindableService#setUpAfterServerImpl(ManagedChannel)}
     * for the child class to access the channel
     * that contacts the server
     * @param bindableServices service to bind to the server
     * @throws IOException if the created server is unable to bind
     */
    protected final void setUpServerImpl(BindableService... bindableServices)
            throws IOException {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();
        // Create server and channel with automatic cleanup
        InProcessServerBuilder builder = InProcessServerBuilder.forName(serverName);
        for (BindableService service : bindableServices) {
            builder.addService(service);
        }
        grpcCleanup.register(builder.directExecutor().build().start());
        ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        setUpAfterServerImpl(channel);
    }

    /**
     * This method is called after creating the
     * server and channel for the test so that
     * the child class can access the given channel
     * @param channel channel to contact the server
     */
    protected abstract void setUpAfterServerImpl(ManagedChannel channel);
}
