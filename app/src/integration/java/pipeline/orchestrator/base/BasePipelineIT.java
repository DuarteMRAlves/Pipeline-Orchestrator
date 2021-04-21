package pipeline.orchestrator.base;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

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
}
