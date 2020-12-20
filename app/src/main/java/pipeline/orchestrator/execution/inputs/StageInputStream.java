package pipeline.orchestrator.execution.inputs;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors.Descriptor;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;

import java.util.function.Supplier;

public interface StageInputStream extends Supplier<ComputationState> {

    boolean isSource();

    static StageInputStream forInputs(
            Descriptor finalMessageDescriptor,
            ImmutableSetMultimap<String, Link> inputs) {

        switch (inputs.size()) {
            case 0:
                // No inputs, source node
                return new SourceInputStream(finalMessageDescriptor);
            case 1:
                return new SingleInputStream(inputs.values().iterator().next());
            default:
                return new MultipleInputStream(finalMessageDescriptor, inputs);
        }
    }

}
