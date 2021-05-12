package pipeline.orchestrator.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class BasePipelineIT {

    protected final Server buildServer(int port, BindableService... services) {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port)
                .directExecutor();
        for (BindableService service : services)
            serverBuilder.addService(service);
        serverBuilder.addService(ProtoReflectionService.newInstance());
        return serverBuilder.build();
    }

    protected final ServerRunner startRunnerForServers(Server... servers) {
        ServerRunner runner = ServerRunner.forServers(servers);
        new Thread(() -> {
            try {
                runner.startAndAwaitTermination();
            } catch (IOException | InterruptedException e) {
                fail(String.format(
                        "Exception caught while running server %s",
                        e));
                Thread.currentThread().interrupt();
            }
        }).start();
        return runner;
    }

    protected final <T> void assertRequestCounter(
            ImmutableSet<T> expectedMessages,
            RequestCounter<T> counter
    ) {
        ImmutableList<T> counterMessages = counter.getReceivedRequests();
        assertTrue(counterMessages.size() <= expectedMessages.size());
        assertTrue(expectedMessages.containsAll(counterMessages));
    }
}
