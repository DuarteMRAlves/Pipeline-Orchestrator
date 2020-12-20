package pipeline.orchestrator.execution.inputs;

import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;

public class SingleInputStream implements StageInputStream {

    private final Link inputLink;

    SingleInputStream(Link inputLink) {
        this.inputLink = inputLink;
    }

    @Override
    public boolean isSource() {
        return false;
    }

    @Override
    public ComputationState get() {
        try {
            return inputLink.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
