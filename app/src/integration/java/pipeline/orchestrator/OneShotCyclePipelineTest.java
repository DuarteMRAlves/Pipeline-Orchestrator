package pipeline.orchestrator;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import pipeline.orchestrator.base.BasePipelineIT;
import pipeline.orchestrator.base.RequestCounter;
import pipeline.orchestrator.base.ServerRunner;
import pipeline.orchestrator.grpc.messages.Data;
import pipeline.orchestrator.services.IncrementService;
import pipeline.orchestrator.services.SourceService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OneShotCyclePipelineTest extends BasePipelineIT {

    private static final String CONFIG_FILE =
            "src/integration/resources/one-shot-cycle-pipeline.yml";

    private static final int NUM_MESSAGES = 3;
    private static final int SOURCE_PORT = 50051;
    private static final int CYCLE1_PORT = 50052;
    private static final int CYClE2_PORT = 50053;

    @Test
    public void testOneShotCyclePipeline() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        RequestCounter.Builder counterBuilder = RequestCounter.newBuilder()
                .setRequestLimit(NUM_MESSAGES);

        RequestCounter<Data> counter1 = counterBuilder.build();
        RequestCounter<Data> counter2 = counterBuilder
                .withCountDownLatch(countDownLatch)
                .build();

        ServerRunner runner = startRunnerForServers(
                buildServer(SOURCE_PORT, new SourceService()),
                buildServer(CYCLE1_PORT, new IncrementService(counter1)),
                buildServer(CYClE2_PORT, new IncrementService(counter2)));

        App app = new App();

        new Thread(() -> app.run(CONFIG_FILE)).start();
        countDownLatch.await();
        app.finish();

        runner.shutdownAndAwaitTermination(5, TimeUnit.SECONDS);

        // Assert messages for cycle1 (should only receive even messages)
        assertRequestCounter(
                ImmutableSet.of(
                        Data.newBuilder().setNum(0).build(),
                        Data.newBuilder().setNum(2).build(),
                        Data.newBuilder().setNum(4).build()
                ),
                counter1
        );

        // Assert messages for cycle2 (should only receive odd messages)
        assertRequestCounter(
                ImmutableSet.of(
                        Data.newBuilder().setNum(1).build(),
                        Data.newBuilder().setNum(3).build(),
                        Data.newBuilder().setNum(5).build()
                ),
                counter2
        );
    }
}
