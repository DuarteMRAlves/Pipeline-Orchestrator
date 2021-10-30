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

public class WaitForStagesIT extends BasePipelineIT {

    private static final String CONFIG_FILE =
            "src/integration/resources/wait-for-stages-pipeline.yml";
    private static final int NUM_SERVICES = 1;
    private static final int NUM_MESSAGES = 3;
    private static final int SOURCE_PORT = 50051;
    private static final int SINK_PORT = 50052;

    @Test
    public void testWaitForStages() throws Exception {
        List<Data> sinkReceived = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(NUM_SERVICES);

        SinkService sinkService = new SinkService(
                NUM_MESSAGES,
                sinkReceived,
                countDownLatch);

        App app = new App();

        new Thread(() -> app.run(CONFIG_FILE)).start();

        // Not pretty, but to ensure the app is running and waiting for the
        // stages to be available.
        Thread.sleep(10000);

        ServerRunner runner = startRunnerForServers(
                buildServer(SOURCE_PORT, new SourceService()),
                buildServer(SINK_PORT, sinkService));

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
