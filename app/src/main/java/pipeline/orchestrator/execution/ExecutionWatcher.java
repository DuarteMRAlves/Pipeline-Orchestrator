package pipeline.orchestrator.execution;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.execution.stages.events.UnavailableServiceEvent;
import pipeline.orchestrator.execution.stages.ExecutionStage;
import pipeline.orchestrator.execution.stages.ExecutionStages;

/**
 * Class to monitor the execution of the stages
 */
public class ExecutionWatcher {

    private static final Logger LOGGER =
            LogManager.getLogger(ExecutionWatcher.class);

    private final ImmutableMap<String, ExecutionStage> stages;

    public ExecutionWatcher(
            ImmutableMap<String, ExecutionStage> stages) {
        this.stages = stages;
        ExecutionStages.subscribeToStagesEvents(this);
    }

    @Subscribe
    public void handleUnavailableService(UnavailableServiceEvent event) {
        LOGGER.warn(
                "Unavailable Service for Stage '{}'",
                event.getStageName());
        // Pause Stages
        LOGGER.info("Pausing stages");
        stages.values().forEach(ExecutionStage::pause);
        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.info("Interrupted");
        }
        // Resume Stages
        LOGGER.info("Resuming stages");
        stages.values().forEach(ExecutionStage::resume);
    }
}
