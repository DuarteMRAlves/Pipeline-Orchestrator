package pipeline.orchestrator.execution.events;

public class UnavailableStageEvent {

    private final String stageName;

    public UnavailableStageEvent(String stageName) {
        this.stageName = stageName;
    }

    public String getStageName() {
        return stageName;
    }

    @Override
    public String toString() {
        return "UnavailableServiceEvent{" +
                "stageName='" + stageName + '\'' +
                '}';
    }
}
