package pipeline.orchestrator.grpc.messages;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.grpc.MethodDescriptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class DynamicMessageMarshaller implements MethodDescriptor.Marshaller<DynamicMessage> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final DynamicMessage.Builder builder;

    private DynamicMessageMarshaller(Descriptors.Descriptor messageDescriptor) {
        builder = DynamicMessage.newBuilder(messageDescriptor);
    }

    /**
     * Creates a new marshaller for messages with the given descriptor
     * @param messageDescriptor descriptor for the messages processed by
     *                          the marshaller
     * @return a new marshaller instance for the given descriptor
     */
    public static DynamicMessageMarshaller forDescriptor(
            Descriptors.Descriptor messageDescriptor) {
        return new DynamicMessageMarshaller(messageDescriptor);
    }

    @Override
    public InputStream stream(DynamicMessage value) {
        return value.toByteString().newInput();
    }

    @Override
    public DynamicMessage parse(InputStream stream) {
        try {
            return builder.clear().mergeFrom(stream).build();
        } catch (IOException e) {
            LOGGER.warn("Unable to parse message from input stream");
            throw new IllegalArgumentException("Unable to parse message from input stream", e);
        }
    }
}
