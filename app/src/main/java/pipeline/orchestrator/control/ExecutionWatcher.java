package pipeline.orchestrator.control;

import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.execution.Execution;
import pipeline.orchestrator.execution.stages.ExecutionStages;
import pipeline.orchestrator.execution.stages.events.UnavailableServiceEvent;

/**
 * Class to monitor the execution of the stages
 */
public class ExecutionWatcher {

    private static final Logger LOGGER =
            LogManager.getLogger(ExecutionWatcher.class);

    private final Execution execution;

    public ExecutionWatcher(Execution execution) {
        this.execution = execution;
        ExecutionStages.subscribeToStagesEvents(this);
    }

    @Subscribe
    public void handleUnavailableService(UnavailableServiceEvent event) {
        LOGGER.warn(
                "Unavailable Service for Stage '{}'",
                event.getStageName());
        // Pause Stages
        LOGGER.info("Pausing execution");
        execution.pause();
        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.info("Interrupted");
        }
        // Resume Stages
        LOGGER.info("Resuming execution");
        execution.resume();
    }
}
