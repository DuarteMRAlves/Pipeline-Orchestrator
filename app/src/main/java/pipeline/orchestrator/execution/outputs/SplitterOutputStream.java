package pipeline.orchestrator.execution.outputs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;
import pipeline.orchestrator.grpc.messages.DynamicMessageSplitter;

import java.util.Map;

class SplitterOutputStream implements StageOutputStream {

    private final DynamicMessageSplitter splitter;
    private final ImmutableSetMultimap<String, Link> outputs;

    public SplitterOutputStream(
            Descriptors.Descriptor descriptor,
            ImmutableSetMultimap<String, Link> outputs) {

        this.outputs = outputs;
        splitter = DynamicMessageSplitter.newBuilder()
                .forDescriptor(descriptor)
                .build();
    }

    static boolean canBuildFrom(
            ImmutableSetMultimap<String, Link> outputs) {

        // Must have all keys not empty and one value per key
        ImmutableSet<String> keys = outputs.keySet();
        return !keys.isEmpty()
                && !keys.contains("")
                && outputs.entries().size() == keys.size();
    }

    @Override
    public void accept(ComputationState computationState) {
        for (Map.Entry<String, Link> pair : outputs.entries()) {
            try {
                DynamicMessage subMessage = splitter.getSubMessage(
                        computationState.getMessage(),
                        pair.getKey());
                pair.getValue().put(ComputationState.from(computationState, subMessage));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
