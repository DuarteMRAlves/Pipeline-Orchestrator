package pipeline.orchestrator.grpc.messages;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.protobuf.Descriptors;

import java.util.Map;

/**
 * Class designed to merge dynamic messages into
 * a new dynamic message with its fields values
 * as the received messages
 */
public class DynamicMessageMerger {

    private final ImmutableMap<String, FieldDescriptor> subMessagesFieldDescriptors;
    private final DynamicMessage.Builder builder;

    public DynamicMessageMerger(Descriptor messageDescriptor) {
        subMessagesFieldDescriptors =
                Descriptors.getSubMessagesFieldDescriptors(messageDescriptor);
        builder = DynamicMessage.newBuilder(messageDescriptor);
    }

    /**
     * Merges the given sub message into the big single message
     * @param subMessages messages to merge
     * @return single message that encapsulates all the received messages. The rest of the
     *         generated message fields will be null
     * @throws NullPointerException if the parameter is null
     */
    public DynamicMessage merge(ImmutableMap<String, DynamicMessage> subMessages) {
        Preconditions.checkNotNull(subMessages);
        builder.clear();
        for (Map.Entry<String, DynamicMessage> messageEntry : subMessages.entrySet()) {
            String fieldName = messageEntry.getKey();
            DynamicMessage fieldValue = messageEntry.getValue();

            Preconditions.checkNotNull(fieldName);
            Preconditions.checkNotNull(fieldValue);

            FieldDescriptor fieldDescriptor = subMessagesFieldDescriptors.get(fieldName);
            Preconditions.checkArgument(
                    fieldDescriptor != null,
                    "Field '%s' does not exists for message",
                    fieldDescriptor,
                    builder.getDescriptorForType().getName());
            builder.setField(fieldDescriptor, messageEntry.getValue());
        }
        return builder.build();
    }

    /**
     * @return a builder to create a new MessageMerger
     */
    public static Builder newBuilder() { return new Builder(); }

    public static class Builder {

        private Descriptor messageDescriptor;

        public Builder forDescriptor(Descriptor descriptor) {
            messageDescriptor = descriptor;
            return this;
        }

        public DynamicMessageMerger build() {
            return new DynamicMessageMerger(messageDescriptor);
        }
    }
}
