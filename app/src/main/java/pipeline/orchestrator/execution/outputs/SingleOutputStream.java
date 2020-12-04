package pipeline.orchestrator.execution.outputs;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.execution.Link;

class SingleOutputStream implements StageOutputStream {

    private final Link output;

    SingleOutputStream(Link output) {
        this.output = output;
    }

    @Override
    public void accept(DynamicMessage dynamicMessage) {
        try {
            output.put(dynamicMessage);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static boolean CanBuildFrom(
            Descriptors.Descriptor receivedMessageDescriptor,
            ImmutableSetMultimap<String, Link> outputs) {

        return outputs.size() == 1;
    }
}
