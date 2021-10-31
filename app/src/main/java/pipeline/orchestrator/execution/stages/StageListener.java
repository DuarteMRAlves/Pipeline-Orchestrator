package pipeline.orchestrator.execution.stages;

public interface StageListener {

    void onUnavailableStage(String stageName);
}
