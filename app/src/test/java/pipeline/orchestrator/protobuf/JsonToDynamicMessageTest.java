package pipeline.orchestrator.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.junit.Test;
import pipeline.orchestrator.grpc.messages.CompositeMessage;
import pipeline.orchestrator.grpc.messages.Data;
import pipeline.orchestrator.grpc.messages.SubMessage;

import static org.junit.Assert.*;

public class JsonToDynamicMessageTest {

    private static final String DATA_JSON = "{ num: 1 }";

    private static final String COMPOSITE_MESSAGE_JSON = "{"
            + "sub_message: { sub_data: { num: 1 } },"
            + "data: { num: 2 },"
            + "value: 3"
            + "}";

    private static final Data DATA_1 = Data.newBuilder()
            .setNum(1)
            .build();

    private static final Data DATA_2 = Data.newBuilder()
            .setNum(2)
            .build();

    private static final SubMessage SUB_MESSAGE = SubMessage.newBuilder()
            .setSubData(DATA_1)
            .build();

    private static final CompositeMessage COMPOSITE_MESSAGE = CompositeMessage.newBuilder()
            .setSubMessage(SUB_MESSAGE)
            .setData(DATA_2)
            .setValue(3)
            .build();

    @Test
    public void parseDataTest() throws Exception {
        // Given
        Descriptors.Descriptor descriptor = Data.getDescriptor();

        // When
        DynamicMessage message = DynamicMessages.parseJson(DATA_JSON, descriptor);

        // Then
        assertEquals(descriptor, message.getDescriptorForType());
        Data created = Data.parseFrom(message.toByteString());
        assertEquals(DATA_1, created);
    }

    @Test
    public void parseCompositeMessageTest() throws Exception {
        // Given
        Descriptors.Descriptor descriptor = CompositeMessage.getDescriptor();

        // When
        DynamicMessage message = DynamicMessages.parseJson(
                COMPOSITE_MESSAGE_JSON,
                descriptor);

        // Then
        assertEquals(descriptor, message.getDescriptorForType());
        CompositeMessage created = CompositeMessage.parseFrom(message.toByteString());
        assertEquals(COMPOSITE_MESSAGE, created);
    }
}
