package pipeline.orchestrator.execution.outputs;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors.Descriptor;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;

import java.util.function.Consumer;

public interface StageOutputStream extends Consumer<ComputationState> {

    static StageOutputStream forOutputs(
            Descriptor receivedMessageDescriptor,
            ImmutableSetMultimap<String, Link> outputs) {

        if (SinkOutputStream.canBuildFrom(outputs)) {
            // No output, sink node
            return new SinkOutputStream();
        }
        else if (SingleOutputStream.canBuildFrom(outputs)) {
            return new SingleOutputStream(outputs.values().iterator().next());
        }
        else if (SplitterOutputStream.canBuildFrom(outputs)) {
            return new SplitterOutputStream(receivedMessageDescriptor, outputs);
        }
        else if (DuplicateOutputStream.canBuildFrom(outputs)) {
            return new DuplicateOutputStream(outputs);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
