package pipeline.orchestrator.grpc.messages;

import com.google.protobuf.DynamicMessage;
import io.grpc.MethodDescriptor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class DynamicMessageMarshallerTest {

    private static final Data ORIGINAL = Data.newBuilder().setNum(2).build();
    private static MethodDescriptor.Marshaller<DynamicMessage> messageMarshaller;

    @BeforeClass
    public static void setUpClass() {
        messageMarshaller = DynamicMessageMarshaller.forDescriptor(Data.getDescriptor());
    }

    @Test
    public void testMarshall() throws Exception {
        DynamicMessage dynamicMessage = DynamicMessage.newBuilder(ORIGINAL).build();
        InputStream stream = messageMarshaller.stream(dynamicMessage);
        Data after = Data.parseFrom(stream);
        assertEquals(ORIGINAL, after);
    }

    @Test
    public void testUnmarshall() {
        DynamicMessage original = DynamicMessage.newBuilder(ORIGINAL).build();
        InputStream stream = ORIGINAL.toByteString().newInput();
        DynamicMessage dynamicMessage = messageMarshaller.parse(stream);
        assertEquals(original, dynamicMessage);
    }

    @Test
    public void testMarshallAndUnmarshall() {
        DynamicMessage original = DynamicMessage.newBuilder(ORIGINAL).build();
        InputStream stream = messageMarshaller.stream(original);
        DynamicMessage dynamicMessage = messageMarshaller.parse(stream);
        assertEquals(original, dynamicMessage);
    }
}
