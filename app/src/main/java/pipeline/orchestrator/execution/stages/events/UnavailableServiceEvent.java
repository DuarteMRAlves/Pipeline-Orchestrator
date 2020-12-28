package pipeline.orchestrator.execution.stages.events;

public class UnavailableServiceEvent {

    private final String stageName;

    public UnavailableServiceEvent(
            String stageName) {

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
