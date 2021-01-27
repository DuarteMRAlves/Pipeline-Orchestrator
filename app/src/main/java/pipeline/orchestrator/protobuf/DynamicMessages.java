package pipeline.orchestrator.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

/**
 * Static helper class to assist with the
 * handling of {@link DynamicMessage}
 */
public class DynamicMessages {

    private DynamicMessages() {}

    /**
     * Parses a Dynamic message from its JSON representation
     * @param json json representation of the message
     * @param descriptor descriptor for the message
     * @return the created message
     */
    public static DynamicMessage parseJson(String json, Descriptors.Descriptor descriptor)
            throws InvalidProtocolBufferException {

        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        // Set the builder fields with the json object attributes
        JsonFormat.parser().merge(json, builder);
        return builder.build();
    }
}
