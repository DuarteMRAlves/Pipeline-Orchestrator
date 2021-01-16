package pipeline.orchestrator.grpc.messages;

import com.google.protobuf.DynamicMessage;
import org.junit.Test;

import static org.junit.Assert.*;

public class DynamicMessageSplitterTest {

    private static final Data DATA_1 = Data.newBuilder()
            .setNum(1)
            .build();

    private static final Data DATA_2 = Data.newBuilder()
            .setNum(2)
            .build();

    private static final SubMessage SUB_MESSAGE = SubMessage.newBuilder()
            .setSubData(DATA_1)
            .build();

    private static final CompositeMessage MESSAGE = CompositeMessage.newBuilder()
            .setSubMessage(SUB_MESSAGE)
            .setData(DATA_2)
            .setValue(3)
            .build();

    private static final DynamicMessageSplitter SPLITTER = DynamicMessageSplitter.newBuilder()
            .forDescriptor(MESSAGE.getDescriptorForType())
            .build();

    @Test
    public void testData() throws Exception {
        final DynamicMessage message = DynamicMessage.newBuilder(MESSAGE).build();
        DynamicMessage value = SPLITTER.getSubMessage(message, "data");
        Data data = Data.parseFrom(value.toByteString());
        assertEquals(DATA_2, data);
        assertEquals(2, data.getNum());
    }

    @Test
    public void testSubMessage() throws Exception {
        final DynamicMessage message = DynamicMessage.newBuilder(MESSAGE).build();
        DynamicMessage value = SPLITTER.getSubMessage(message, "sub_message");
        SubMessage subMessage = SubMessage.parseFrom(value.toByteString());
        assertEquals(SUB_MESSAGE, subMessage);
        assertEquals(DATA_1, subMessage.getSubData());
        assertEquals(1, subMessage.getSubData().getNum());
    }

    @Test
    public void testDefaultFieldValue() throws Exception {
        CompositeMessage compositeMessage = CompositeMessage.newBuilder().build();
        final DynamicMessage message = DynamicMessage.newBuilder(compositeMessage).build();
        DynamicMessage value = SPLITTER.getSubMessage(message, "sub_message");
        SubMessage subMessage = SubMessage.parseFrom(value.toByteString());
        assertEquals(SubMessage.getDefaultInstance(), subMessage);
        assertEquals(Data.getDefaultInstance(), subMessage.getSubData().getDefaultInstanceForType());
    }

    @Test
    public void testNotMessageField() {
        final DynamicMessage message = DynamicMessage.newBuilder(MESSAGE).build();
        assertThrows(IllegalArgumentException.class,
                     () -> SPLITTER.getSubMessage(message, "value"));
    }

    @Test
    public void testNoSuchField() {
        final DynamicMessage message = DynamicMessage.newBuilder(MESSAGE).build();
        assertThrows(IllegalArgumentException.class,
                () -> SPLITTER.getSubMessage(message, "no_such_field"));
    }

    @Test
    public void testInvalidMessageType() {
        final DynamicMessage message = DynamicMessage.newBuilder(SUB_MESSAGE).build();
        assertThrows(IllegalArgumentException.class,
                () -> SPLITTER.getSubMessage(message, "sub_data"));
    }

    @Test
    public void testNullMessage() {
        assertThrows(NullPointerException.class,
                () -> SPLITTER.getSubMessage(null, "sub_message"));
    }

    @Test
    public void testNullFieldName() {
        final DynamicMessage message = DynamicMessage.newBuilder(SUB_MESSAGE).build();
        assertThrows(NullPointerException.class,
                () -> SPLITTER.getSubMessage(message, null));
    }
}
