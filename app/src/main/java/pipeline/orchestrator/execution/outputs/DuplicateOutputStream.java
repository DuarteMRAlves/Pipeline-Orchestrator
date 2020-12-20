package pipeline.orchestrator.execution.outputs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;


public class DuplicateOutputStream implements StageOutputStream {

    private final ImmutableSet<Link> outputs;

    public DuplicateOutputStream(
            ImmutableSetMultimap<String, Link> outputs) {

        Preconditions.checkArgument(canBuildFrom(outputs));
        this.outputs = outputs.get("");
    }

    static boolean canBuildFrom(
            ImmutableSetMultimap<String, Link> outputs) {

        // Only contains empty key
        ImmutableSet<String> keySet = outputs.keySet();
        return keySet.size() == 1 && keySet.contains("");
    }

    @Override
    public void accept(ComputationState computationState) {
        for (Link link : outputs) {
            try {
                link.put(computationState);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public String toString() {
        return "DuplicateOutputStream {"
                + "numOutputs: " + outputs.size() + "}";
    }
}
