package pipeline.orchestrator.execution.outputs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.execution.Link;


public class DuplicateOutputStream implements StageOutputStream {

    private final ImmutableSet<Link> outputs;

    public DuplicateOutputStream(
            ImmutableSetMultimap<String, Link> outputs) {

        Preconditions.checkArgument(CanBuildFrom(null, outputs));
        this.outputs = outputs.get("");
    }

    static boolean CanBuildFrom(
            Descriptors.Descriptor receivedMessageDescriptor,
            ImmutableSetMultimap<String, Link> outputs) {

        // Only contains empty key
        ImmutableSet<String> keySet = outputs.keySet();
        return keySet.size() == 1 && keySet.contains("");
    }

    @Override
    public void accept(DynamicMessage dynamicMessage) {
        for (Link link : outputs) {
            try {
                link.put(dynamicMessage);
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
