package pipeline.orchestrator.architecture.parsing;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.grpc.MethodDescriptor;
import org.junit.Test;
import pipeline.orchestrator.verification.exceptions.NotNullVerificationException;

import static org.junit.Assert.*;

public class YamlParseMethodInformationTest {

    private static final String NAME = "Name 1";

    private static final VerifyObjectMapper MAPPER = new VerifyObjectMapper(new YAMLFactory());

    @Test
    public void unaryMethodTest() throws Exception {
        String content = "name: \"" + NAME + "\"\n"
                + "type: UNARY";

        MethodInformationDto methodInformation = MAPPER.readValue(
                content,
                MethodInformationDto.class);

        assertEquals(NAME, methodInformation.getName());
        assertEquals(MethodDescriptor.MethodType.UNARY, methodInformation.getType());
    }

    @Test
    public void clientStreamingMethodTest() throws Exception {
        String content = "name: \"" + NAME + "\"\n"
                + "type: CLIENT_STREAMING";

        MethodInformationDto methodInformation = MAPPER.readValue(
                content,
                MethodInformationDto.class);

        assertEquals(NAME, methodInformation.getName());
        assertEquals(MethodDescriptor.MethodType.CLIENT_STREAMING, methodInformation.getType());
    }

    @Test
    public void serverStreamingMethodTest() throws Exception {
        String content = "name: \"" + NAME + "\"\n"
                + "type: SERVER_STREAMING";

        MethodInformationDto methodInformation = MAPPER.readValue(
                content,
                MethodInformationDto.class);

        assertEquals(NAME, methodInformation.getName());
        assertEquals(MethodDescriptor.MethodType.SERVER_STREAMING, methodInformation.getType());
    }

    @Test
    public void bidirectionalStreamingMethodTest() throws Exception {
        String content = "name: \"" + NAME + "\"\n"
                + "type: BIDI_STREAMING";

        MethodInformationDto methodInformation = MAPPER.readValue(
                content,
                MethodInformationDto.class);

        assertEquals(NAME, methodInformation.getName());
        assertEquals(MethodDescriptor.MethodType.BIDI_STREAMING, methodInformation.getType());
    }

    @Test
    public void missingNameTest() throws Exception {
        String content = "type: UNARY";

        MethodInformationDto methodInformation = MAPPER.readValue(
                content,
                MethodInformationDto.class);

        assertNull(methodInformation.getName());
        assertEquals(MethodDescriptor.MethodType.UNARY, methodInformation.getType());
    }

    @Test
    public void missingTypeTest() {
        String content = "name: \"" + NAME + "\"";

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> MAPPER.readValue(content, MethodInformationDto.class));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "type"),
                exception.getMessage());
    }
}