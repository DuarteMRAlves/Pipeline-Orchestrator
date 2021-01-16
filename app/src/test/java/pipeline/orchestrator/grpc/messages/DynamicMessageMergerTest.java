package pipeline.orchestrator.grpc.messages;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DynamicMessage;
import org.junit.Test;

import static org.junit.Assert.*;

public class DynamicMessageMergerTest {

    private static final Data DATA_1 = Data.newBuilder()
            .setNum(1)
            .build();

    private static final Data DATA_2 = Data.newBuilder()
            .setNum(2)
            .build();

    private static final SubMessage SUB_MESSAGE = SubMessage.newBuilder()
            .setSubData(DATA_1)
            .build();

    private static final DynamicMessageMerger MERGER = DynamicMessageMerger.newBuilder()
            .forDescriptor(CompositeMessage.getDescriptor())
            .build();

    @Test
    public void testDataOnly() throws Exception {
        final ImmutableMap<String, DynamicMessage> subMessages =
                ImmutableMap.<String, DynamicMessage>builder()
                        .put("data", DynamicMessage.newBuilder(DATA_2).build())
                        .build();

        final DynamicMessage compositeMessage = MERGER.merge(subMessages);

        CompositeMessage expected = CompositeMessage.newBuilder()
                .setData(DATA_2)
                .build();
        CompositeMessage result = CompositeMessage.parseFrom(compositeMessage.toByteString());

        assertEquals(expected, result);
        assertEquals(DATA_2, result.getData());
        assertEquals(SubMessage.getDefaultInstance(), result.getSubMessage());
        assertEquals(0, result.getValue());
    }

    @Test
    public void testSubMessageOnly() throws Exception {
        final ImmutableMap<String, DynamicMessage> subMessages =
                ImmutableMap.<String, DynamicMessage>builder()
                        .put("sub_message", DynamicMessage.newBuilder(SUB_MESSAGE).build())
                        .build();

        final DynamicMessage compositeMessage = MERGER.merge(subMessages);

        CompositeMessage expected = CompositeMessage.newBuilder()
                .setSubMessage(SUB_MESSAGE)
                .build();
        CompositeMessage result = CompositeMessage.parseFrom(compositeMessage.toByteString());

        assertEquals(expected, result);
        assertEquals(Data.getDefaultInstance(), result.getData());
        assertEquals(SUB_MESSAGE, result.getSubMessage());
        assertEquals(DATA_1, result.getSubMessage().getSubData());
        assertEquals(0, result.getValue());
    }

    @Test
    public void testDataAndSubMessage() throws Exception {
        final ImmutableMap<String, DynamicMessage> subMessages =
                ImmutableMap.<String, DynamicMessage>builder()
                        .put("sub_message", DynamicMessage.newBuilder(SUB_MESSAGE).build())
                        .put("data", DynamicMessage.newBuilder(DATA_2).build())
                        .build();

        final DynamicMessage compositeMessage = MERGER.merge(subMessages);

        CompositeMessage expected = CompositeMessage.newBuilder()
                .setSubMessage(SUB_MESSAGE)
                .setData(DATA_2)
                .build();
        CompositeMessage result = CompositeMessage.parseFrom(compositeMessage.toByteString());

        assertEquals(expected, result);
        assertEquals(DATA_2, result.getData());
        assertEquals(SUB_MESSAGE, result.getSubMessage());
        assertEquals(DATA_1, result.getSubMessage().getSubData());
        assertEquals(0, result.getValue());
    }

    @Test
    public void testNullSubMessages() {
        assertThrows(NullPointerException.class, () -> MERGER.merge(null));
    }

    @Test
    public void testNoSuchMessage() {
        final ImmutableMap<String, DynamicMessage> subMessages =
                ImmutableMap.<String, DynamicMessage>builder()
                        .put("no_such_field", DynamicMessage.newBuilder(SUB_MESSAGE).build())
                        .put("data", DynamicMessage.newBuilder(DATA_2).build())
                        .build();
        assertThrows(IllegalArgumentException.class, () -> MERGER.merge(subMessages));
    }
}
