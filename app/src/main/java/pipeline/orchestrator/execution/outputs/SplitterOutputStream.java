package pipeline.orchestrator.execution.outputs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import pipeline.core.messages.MessageSplitter;
import pipeline.orchestrator.execution.Link;

import java.util.Map;

class SplitterOutputStream implements StageOutputStream {

    private final MessageSplitter splitter;
    private final ImmutableSetMultimap<String, Link> outputs;

    public SplitterOutputStream(
            Descriptors.Descriptor descriptor,
            ImmutableSetMultimap<String, Link> outputs) {

        this.outputs = outputs;
        splitter = MessageSplitter.newBuilder()
                .forDescriptor(descriptor)
                .build();
    }

    static boolean CanBuildFrom(
            Descriptors.Descriptor receivedMessageDescriptor,
            ImmutableSetMultimap<String, Link> outputs) {

        // Must have all keys not empty and one value per key
        ImmutableSet<String> keys = outputs.keySet();
        return keys.size() > 1
                && !keys.contains("")
                && outputs.entries().size() == keys.size();
    }

    @Override
    public void accept(DynamicMessage dynamicMessage) {
        for (Map.Entry<String, Link> pair : outputs.entries()) {
            try {
                pair.getValue().put(splitter.getSubMessage(dynamicMessage, pair.getKey()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
