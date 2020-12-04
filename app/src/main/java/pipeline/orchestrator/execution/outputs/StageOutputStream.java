package pipeline.orchestrator.execution.outputs;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.execution.Link;

import java.util.function.Consumer;

public interface StageOutputStream extends Consumer<DynamicMessage> {

    static StageOutputStream forOutputs(
            Descriptor receivedMessageDescriptor,
            ImmutableSetMultimap<String, Link> outputs) {

        if (SinkOutputStream.CanBuildFrom(receivedMessageDescriptor, outputs)) {
            // No output, sink node
            return new SinkOutputStream();
        }
        else if (SingleOutputStream.CanBuildFrom(receivedMessageDescriptor, outputs)) {
            return new SingleOutputStream(outputs.values().iterator().next());
        }
        else if (SplitterOutputStream.CanBuildFrom(receivedMessageDescriptor, outputs)) {
            return new SplitterOutputStream(receivedMessageDescriptor, outputs);
        }
        else if (DuplicateOutputStream.CanBuildFrom(receivedMessageDescriptor, outputs)) {
            return new DuplicateOutputStream(outputs);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
