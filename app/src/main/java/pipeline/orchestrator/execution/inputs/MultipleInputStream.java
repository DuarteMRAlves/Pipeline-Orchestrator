package pipeline.orchestrator.execution.inputs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import pipeline.core.messages.MessageMerger;
import pipeline.orchestrator.execution.Link;

import java.util.Map;

public class MultipleInputStream implements StageInputStream {

    private final MessageMerger merger;
    private final ImmutableSetMultimap<String, Link> inputs;

    MultipleInputStream(
            Descriptors.Descriptor descriptor,
            ImmutableSetMultimap<String, Link> inputs) {
        this.inputs = inputs;
        merger = MessageMerger.newBuilder()
                .forDescriptor(descriptor)
                .build();
    }

    @Override
    public DynamicMessage get() {
        ImmutableMap.Builder<String, DynamicMessage> builder = ImmutableMap.builder();
        for (Map.Entry<String, Link> pair : inputs.entries()) {
            try {
                builder.put(pair.getKey(), pair.getValue().take());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return merger.merge(builder.build());
    }
}
