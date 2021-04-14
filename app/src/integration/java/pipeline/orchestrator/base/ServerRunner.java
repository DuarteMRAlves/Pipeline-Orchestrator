package pipeline.orchestrator.base;

import io.grpc.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Class to run multiple servers
 */
public class ServerRunner {

    private final Server[] servers;

    private ServerRunner(Server[] servers) {
        this.servers = servers;
    }

    public static ServerRunner forServers(Server... servers) {
        return new ServerRunner(servers);
    }

    public void startAndAwaitTermination() throws
            IOException,
            InterruptedException {
        for (Server server : servers) {
            server.start();
        }
        for (Server server : servers) {
            server.awaitTermination();
        }
    }

    public void shutdownAndAwaitTermination(
            long timeout,
            TimeUnit timeUnit
    ) throws InterruptedException {
        for (Server server : servers) {
            server.shutdown();
        }
        for (Server server : servers) {
            server.awaitTermination(timeout, timeUnit);
        }
    }
}
