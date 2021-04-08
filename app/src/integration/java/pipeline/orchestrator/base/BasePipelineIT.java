package pipeline.orchestrator.base;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

public abstract class BasePipelineIT {

    protected final Server buildServer(int port, BindableService... services) {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port)
                .directExecutor();
        for (BindableService service : services)
            serverBuilder.addService(service);
        serverBuilder.addService(ProtoReflectionService.newInstance());
        return serverBuilder.build();
    }
}
