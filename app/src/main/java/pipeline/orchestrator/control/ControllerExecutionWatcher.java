package pipeline.orchestrator.control;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.execution.Execution;
import pipeline.orchestrator.execution.ExecutionWatcher;
import pipeline.orchestrator.execution.events.UnavailableStageEvent;

/**
 * Class to monitor the execution of the stages
 */
public class ControllerExecutionWatcher implements ExecutionWatcher {

    private static final Logger LOGGER =
            LogManager.getLogger(ControllerExecutionWatcher.class);

    private final Execution execution;

    public ControllerExecutionWatcher(Execution execution) {
        this.execution = execution;
        execution.registerWatcher(this);
    }

    @Override
    public void onUnavailableStage(UnavailableStageEvent event) {
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
