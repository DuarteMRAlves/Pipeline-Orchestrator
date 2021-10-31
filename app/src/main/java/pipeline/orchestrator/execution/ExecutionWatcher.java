package pipeline.orchestrator.execution;

import pipeline.orchestrator.execution.events.UnavailableStageEvent;

public interface ExecutionWatcher {

    void onUnavailableStage(UnavailableStageEvent event);
}
