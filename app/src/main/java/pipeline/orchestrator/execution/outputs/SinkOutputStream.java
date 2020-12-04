package pipeline.orchestrator.execution.outputs;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.execution.Link;

class SinkOutputStream implements StageOutputStream {

    static boolean CanBuildFrom(
            Descriptors.Descriptor receivedMessageDescriptor,
            ImmutableSetMultimap<String, Link> outputs) {
        return outputs.isEmpty();
    }

    @Override
    public void accept(DynamicMessage dynamicMessage) {
        // Do nothing, just drop message
    }
}
