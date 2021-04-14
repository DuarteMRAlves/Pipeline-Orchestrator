package pipeline.orchestrator.architecture.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import pipeline.orchestrator.verification.Verifications;
import pipeline.orchestrator.verification.exceptions.NotNullVerificationException;
import pipeline.orchestrator.verification.exceptions.PositiveVerificationException;

import static org.junit.Assert.*;

public class YamlParseStageInformationTest {

    private static final String NAME = "Name";

    private static final String HOST = "Host";

    private static final int PORT = 1;

    private static final String SERVICE_NAME = "Service";

    private static final String METHOD_NAME = "Method";

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    @Test
    public void allFieldsTest() throws Exception {
        String content =
                "name: \"" + NAME + "\"\n" +
                "host: " + HOST + "\n" +
                "port: " + PORT + "\n" +
                "service: " + SERVICE_NAME + "\n" +
                "method: " + METHOD_NAME + "\n";

        StageInformationDto stageInformation = MAPPER.readValue(
                content,
                StageInformationDto.class);
        // Nothing should happen
        Verifications.verify(stageInformation);

        assertEquals(NAME, stageInformation.getName());
        assertEquals(HOST, stageInformation.getHost());
        assertEquals(PORT, stageInformation.getPort());
        assertEquals(SERVICE_NAME, stageInformation.getService());
        assertEquals(METHOD_NAME, stageInformation.getMethod());
    }

    @Test
    public void noMethodNameTest() throws Exception {
        String content =
                "name: \"" + NAME + "\"\n" +
                "host: " + HOST + "\n" +
                "port: " + PORT + "\n";

        StageInformationDto stageInformation = MAPPER.readValue(
                content,
                StageInformationDto.class);
        // Nothing should happen
        Verifications.verify(stageInformation);

        assertEquals(NAME, stageInformation.getName());
        assertEquals(HOST, stageInformation.getHost());
        assertEquals(PORT, stageInformation.getPort());
        assertNull(stageInformation.getService());
        assertNull(stageInformation.getMethod());
    }

    @Test
    public void missingNameFieldTest() throws Exception {
        String content =
                "host: " + HOST + "\n" +
                "port: " + PORT + "\n" +
                "service: " + SERVICE_NAME + "\n" +
                "method: " + METHOD_NAME + "\n";

        StageInformationDto stageInformation = MAPPER.readValue(
                content,
                StageInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () ->  Verifications.verify(stageInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "name"),
                exception.getMessage());
    }

    @Test
    public void missingHostFieldTest() throws Exception {
        String content =
                "name: \"" + NAME + "\"\n" +
                "port: " + PORT + "\n" +
                "service: " + SERVICE_NAME + "\n" +
                "method: " + METHOD_NAME + "\n";

        StageInformationDto stageInformation = MAPPER.readValue(
                content,
                StageInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () ->  Verifications.verify(stageInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "host"),
                exception.getMessage());
    }

    @Test
    public void nonPositivePortTest() throws Exception {
        String content =
                "name: \"" + NAME + "\"\n" +
                "host: " + HOST + "\n" +
                "port: " + 0 + "\n" +
                "service: " + SERVICE_NAME + "\n" +
                "method: " +  METHOD_NAME + "\n";

        StageInformationDto stageInformation = MAPPER.readValue(
                content,
                StageInformationDto.class);

        PositiveVerificationException exception = assertThrows(
                PositiveVerificationException.class,
                () -> Verifications.verify(stageInformation));

        assertEquals(
                String.format(PositiveVerificationException.MESSAGE, "port"),
                exception.getMessage());
    }
}
