package pipeline.orchestrator;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import pipeline.orchestrator.base.BasePipelineIT;
import pipeline.orchestrator.base.ServerRunner;
import pipeline.orchestrator.grpc.messages.Data;
import pipeline.orchestrator.services.SinkService;
import pipeline.orchestrator.services.SourceService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class SameServerLinearPipelineIT extends BasePipelineIT {

    private static final String CONFIG_FILE =
            "src/integration/resources/same-server-linear-pipeline.yml";

    private static final int NUM_SERVICES_TO_WAIT = 1;
    private static final int NUM_MESSAGES = 3;
    private static final int SERVER_PORT = 50051;

    @Test
    public void testSameServerPipeline() throws Exception {
        List<Data> sinkReceived = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(NUM_SERVICES_TO_WAIT);

        SinkService sinkService = new SinkService(
                NUM_MESSAGES,
                sinkReceived,
                countDownLatch);

        ServerRunner runner = startRunnerForServers(
                buildServer(SERVER_PORT, new SourceService(), sinkService));

        App app = new App();

        new Thread(() -> app.run(CONFIG_FILE)).start();
        countDownLatch.await();
        app.finish();

        runner.shutdownAndAwaitTermination(5, TimeUnit.SECONDS);

        // Assert messages
        assertTrue(sinkReceived.size() <= NUM_MESSAGES);
        assertTrue(
                ImmutableSet.of(
                        Data.newBuilder().setNum(0).build(),
                        Data.newBuilder().setNum(1).build(),
                        Data.newBuilder().setNum(2).build()
                ).containsAll(sinkReceived)
        );
    }
}
