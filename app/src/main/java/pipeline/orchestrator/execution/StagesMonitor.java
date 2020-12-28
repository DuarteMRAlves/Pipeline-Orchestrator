package pipeline.orchestrator.execution;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.execution.stages.events.UnavailableServiceEvent;
import pipeline.orchestrator.execution.stages.AbstractPipelineStage;
import pipeline.orchestrator.execution.stages.PipelineStages;

/**
 * Class to monitor the execution of the stages
 */
public class StagesMonitor {

    private final Logger LOGGER = LogManager.getLogger(StagesMonitor.class);

    private final ImmutableMap<String, AbstractPipelineStage> stages;

    public StagesMonitor(
            ImmutableMap<String, AbstractPipelineStage> stages) {
        this.stages = stages;
        PipelineStages.subscribeToStagesEvents(this);
    }

    @Subscribe
    public void handleUnavailableService(UnavailableServiceEvent event) {
        LOGGER.warn(
                "Unavailable Service for Stage '{}'",
                event.getStageName());
        System.exit(1);
    }
}
