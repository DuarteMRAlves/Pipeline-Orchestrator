package pipeline.orchestrator.architecture.parsing;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import pipeline.orchestrator.verification.exceptions.NotNullVerificationException;

import static org.junit.Assert.*;

public class YamlParseLinkInformationTest {

    private static final String SOURCE_STAGE = "Source Stage";

    private static final String TARGET_STAGE = "Target Stage";

    private static final String SOURCE_FIELD = "Source Field";

    private static final String TARGET_FIELD = "Target Field";

    private static final VerifyObjectMapper MAPPER = new VerifyObjectMapper(new YAMLFactory());

    @Test
    public void allFieldsTest() throws Exception {
        String content =
                "source:\n" +
                "  stage: \"" + SOURCE_STAGE + "\"\n" +
                "  field: \"" + SOURCE_FIELD + "\"\n" +
                "target:\n" +
                "  stage: \"" + TARGET_STAGE + "\"\n" +
                "  field: \"" + TARGET_FIELD + "\"\n";

        LinkInformationDto linkInformation = MAPPER.readValue(
                content,
                LinkInformationDto.class);

        assertEquals(SOURCE_STAGE, linkInformation.getSource().getStage());
        assertEquals(SOURCE_FIELD, linkInformation.getSource().getField());
        assertEquals(TARGET_STAGE, linkInformation.getTarget().getStage());
        assertEquals(TARGET_FIELD, linkInformation.getTarget().getField());
    }

    @Test
    public void onlyStageFieldsTest() throws Exception {
        String content =
                "source:\n" +
                "  stage: \"" + SOURCE_STAGE + "\"\n" +
                "target:\n" +
                "  stage: \"" + TARGET_STAGE + "\"\n";

        LinkInformationDto linkInformation = MAPPER.readValue(
                content,
                LinkInformationDto.class);

        assertEquals(SOURCE_STAGE, linkInformation.getSource().getStage());
        assertNull(linkInformation.getSource().getField());
        assertEquals(TARGET_STAGE, linkInformation.getTarget().getStage());
        assertNull(linkInformation.getTarget().getField());
    }

    @Test
    public void missingSourceTest() {
        String content =
                "target:\n" +
                "  stage: \"" + TARGET_STAGE + "\"\n" +
                "  field: \"" + TARGET_FIELD + "\"\n";

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> MAPPER.readValue(content, LinkInformationDto.class));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "source"),
                exception.getMessage());
    }

    @Test
    public void missingTargetTest() {
        String content =
                "source:\n" +
                "  stage: \"" + SOURCE_STAGE + "\"\n" +
                "  field: \"" + SOURCE_FIELD + "\"\n";

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> MAPPER.readValue(content, LinkInformationDto.class));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "target"),
                exception.getMessage());
    }

    @Test
    public void missingSourceStageTest() {
        String content =
                "source:\n" +
                "  field: \"" + SOURCE_FIELD + "\"\n" +
                "target:\n" +
                "  stage: \"" + TARGET_STAGE + "\"\n" +
                "  field: \"" + TARGET_FIELD + "\"\n";

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> MAPPER.readValue(content, LinkInformationDto.class));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "stage"),
                exception.getMessage());
    }

    @Test
    public void missingTargetStageTest() {
        String content =
                "source:\n" +
                "  stage: \"" + SOURCE_STAGE + "\"\n" +
                "  field: \"" + SOURCE_FIELD + "\"\n" +
                "target:\n" +
                "  field: \"" + TARGET_FIELD + "\"\n";

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> MAPPER.readValue(content, LinkInformationDto.class));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "stage"),
                exception.getMessage());
    }
}
