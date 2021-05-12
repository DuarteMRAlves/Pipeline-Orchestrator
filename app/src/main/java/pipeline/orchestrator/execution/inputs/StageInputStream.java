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

        if (SourceInputStream.canBuildFrom(inputs)) {
            // No inputs, source node
            return new SourceInputStream(finalMessageDescriptor);
        } else if (SingleInputStream.canBuildFrom(inputs)) {
            return new SingleInputStream(inputs.values().iterator().next());
        } else if (CollectorInputStream.canBuildFrom(inputs)) {
            return new CollectorInputStream(inputs);
        } else if (MultipleInputStream.canBuildFrom(inputs)) {
            return new MultipleInputStream(finalMessageDescriptor, inputs);
        } else {
            throw new IllegalArgumentException();
        }
    }

}
