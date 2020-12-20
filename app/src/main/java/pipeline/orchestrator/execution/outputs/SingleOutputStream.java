package pipeline.orchestrator.execution.outputs;

import com.google.common.collect.ImmutableSetMultimap;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;

class SingleOutputStream implements StageOutputStream {

    private final Link output;

    SingleOutputStream(Link output) {
        this.output = output;
    }

    @Override
    public void accept(ComputationState state) {
        try {
            output.put(state);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static boolean canBuildFrom(
            ImmutableSetMultimap<String, Link> outputs) {

        return outputs.size() == 1
                && outputs.keys().contains("");
    }
}
