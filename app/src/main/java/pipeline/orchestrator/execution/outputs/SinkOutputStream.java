package pipeline.orchestrator.execution.outputs;

import com.google.common.collect.ImmutableSetMultimap;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;

class SinkOutputStream implements StageOutputStream {

    static boolean canBuildFrom(
            ImmutableSetMultimap<String, Link> outputs) {
        return outputs.isEmpty();
    }

    @Override
    public void accept(ComputationState state) {
        // Do nothing, just drop message
    }
}
