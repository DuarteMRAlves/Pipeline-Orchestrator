package pipeline.orchestrator.grpc.messages;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.protobuf.Descriptors;

/**
 * Class designed to recover sub messages from dynamic messages
 */
public class DynamicMessageSplitter {

    private final Descriptor messageDescriptor;
    private final ImmutableMap<String, FieldDescriptor> subMessagesFieldDescriptors;

    public DynamicMessageSplitter(Descriptor messageDescriptor) {
        this.messageDescriptor = messageDescriptor;
        this.subMessagesFieldDescriptors =
                Descriptors.getSubMessagesFieldDescriptors(messageDescriptor);
    }

    /**
     * Gets a sub message field for the given message with the given field name
     * @param message message with returned sub message field
     * @param fieldName name of sub message field
     * @return the sub message value or the default value for the field if it was not set
     * @throws NullPointerException if message, field name are null
     * @throws IllegalArgumentException if the splitter descriptor is different from the
     *                                  message descriptor, if no such field exists in the message
     *                                  or the field is not a message
     */
    public DynamicMessage getSubMessage(DynamicMessage message, String fieldName) {
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(fieldName);
        Preconditions.checkArgument(message.getDescriptorForType().equals(messageDescriptor));
        Preconditions.checkArgument(subMessagesFieldDescriptors.containsKey(fieldName));

        Object fieldValue = message.getField(subMessagesFieldDescriptors.get(fieldName));
        if (fieldValue instanceof DynamicMessage)
            return (DynamicMessage) fieldValue;
        else
            throw new IllegalArgumentException();
    }

    public static Builder newBuilder() { return new Builder(); }

    public static class Builder {

        private Descriptor messageDescriptor;

        public Builder forDescriptor(Descriptor descriptor) {
            messageDescriptor = descriptor;
            return this;
        }

        public DynamicMessageSplitter build() {
            return new DynamicMessageSplitter(messageDescriptor);
        }
    }
}
