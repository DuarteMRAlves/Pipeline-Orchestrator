package pipeline.orchestrator.execution.inputs;

import com.google.common.collect.ImmutableSetMultimap;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;

public class SingleInputStream implements StageInputStream {

    private final Link inputLink;

    SingleInputStream(Link inputLink) {
        this.inputLink = inputLink;
    }

    static boolean canBuildFrom(ImmutableSetMultimap<String, Link> inputs) {
        // Single output with no specific field
        return inputs.size() == 1 && inputs.keys().contains("");
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
