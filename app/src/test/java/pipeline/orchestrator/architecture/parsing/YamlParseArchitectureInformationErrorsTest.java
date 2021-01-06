package pipeline.orchestrator.architecture.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.grpc.MethodDescriptor;
import org.junit.Test;
import pipeline.orchestrator.verification.Verifications;
import pipeline.orchestrator.verification.exceptions.NotNullVerificationException;

import java.util.List;

import static org.junit.Assert.*;

public class YamlParseArchitectureInformationErrorsTest {

    private static final String STAGE_NAME_1 = "Stage Name 1";
    private static final String HOST_1 = "Host1";
    private static final int PORT_1 = 1;
    private static final String METHOD_NAME_1 = "Method Name 1";

    private static final String STAGE_NAME_2 = "Stage Name 2";
    private static final String HOST_2 = "Host2";
    private static final int PORT_2 = 2;
    private static final String METHOD_NAME_2 = "Method Name 2";

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    @Test
    public void allFieldsTest() throws Exception {
        String content =
                "stages:\n" +
                "-  name: \"" + STAGE_NAME_1 + "\"\n" +
                "   host: " + HOST_1 + "\n" +
                "   port: " + PORT_1 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_1 + "\"\n" +
                "      type: UNARY\n" +
                "-  name: \"" + STAGE_NAME_2 + "\"\n" +
                "   host: " + HOST_2 + "\n" +
                "   port: " + PORT_2 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_2 + "\"\n" +
                "      type: UNARY\n" +
                "links:\n" +
                "-  source:\n" +
                "      stage: \"" + STAGE_NAME_1 + "\"\n" +
                "   target:\n" +
                "      stage: \"" + STAGE_NAME_2 + "\"\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        // Nothing should happen
        Verifications.verify(architectureInformation);


        List<StageInformationDto> stages = architectureInformation.getStages();
        assertEquals(2, stages.size());

        StageInformationDto stage1 = stages.get(0);
        assertEquals(STAGE_NAME_1, stage1.getName());
        assertEquals(HOST_1, stage1.getHost());
        assertEquals(PORT_1, stage1.getPort());
        assertEquals(METHOD_NAME_1, stage1.getMethod().getName());
        assertEquals(MethodDescriptor.MethodType.UNARY, stage1.getMethod().getType());


        StageInformationDto stage2 = stages.get(1);
        assertEquals(STAGE_NAME_2, stage2.getName());
        assertEquals(HOST_2, stage2.getHost());
        assertEquals(PORT_2, stage2.getPort());
        assertEquals(METHOD_NAME_2, stage2.getMethod().getName());
        assertEquals(MethodDescriptor.MethodType.UNARY, stage2.getMethod().getType());

        List<LinkInformationDto> links = architectureInformation.getLinks();
        assertEquals(1, links.size());

        LinkInformationDto link = links.get(0);
        assertEquals(STAGE_NAME_1, link.getSource().getStage());
        assertNull(link.getSource().getField());
        assertEquals(STAGE_NAME_2, link.getTarget().getStage());
        assertNull(link.getTarget().getField());
    }

    @Test
    public void missingStagesTest() throws Exception {
        String content =
                "links:\n" +
                "-  source:\n" +
                "      stage: \"" + STAGE_NAME_1 + "\"\n" +
                "   target:\n" +
                "      stage: \"" + STAGE_NAME_2 + "\"\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> Verifications.verify(architectureInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "stages"),
                exception.getMessage());
    }

    @Test
    public void emptyStagesTest() throws Exception {
        String content =
                "stages:\n" +
                "links:\n" +
                "-  source:\n" +
                "      stage: \"" + STAGE_NAME_1 + "\"\n" +
                "   target:\n" +
                "      stage: \"" + STAGE_NAME_2 + "\"\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> Verifications.verify(architectureInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "stages"),
                exception.getMessage());
    }

    @Test
    public void missingLinksTest() throws Exception {
        String content =
                "stages:\n" +
                "-  name: \"" + STAGE_NAME_1 + "\"\n" +
                "   host: " + HOST_1 + "\n" +
                "   port: " + PORT_1 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_1 + "\"\n" +
                "      type: UNARY\n" +
                "-  name: \"" + STAGE_NAME_2 + "\"\n" +
                "   host: " + HOST_2 + "\n" +
                "   port: " + PORT_2 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_2 + "\"\n" +
                "      type: UNARY\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> Verifications.verify(architectureInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "links"),
                exception.getMessage());
    }

    @Test
    public void emptyLinksTest() throws Exception {
        String content =
                "stages:\n" +
                "-  name: \"" + STAGE_NAME_1 + "\"\n" +
                "   host: " + HOST_1 + "\n" +
                "   port: " + PORT_1 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_1 + "\"\n" +
                "      type: UNARY\n" +
                "-  name: \"" + STAGE_NAME_2 + "\"\n" +
                "   host: " + HOST_2 + "\n" +
                "   port: " + PORT_2 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_2 + "\"\n" +
                "      type: UNARY\n" +
                "links:\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> Verifications.verify(architectureInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "links"),
                exception.getMessage());
    }

    @Test
    public void missingStageNameTest() throws Exception {
        String content =
                "stages:\n" +
                "-  host: " + HOST_1 + "\n" +
                "   port: " + PORT_1 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_1 + "\"\n" +
                "      type: UNARY\n" +
                "-  name: \"" + STAGE_NAME_2 + "\"\n" +
                "   host: " + HOST_2 + "\n" +
                "   port: " + PORT_2 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_2 + "\"\n" +
                "      type: UNARY\n" +
                "links:\n" +
                "-  source:\n" +
                "      stage: \"" + STAGE_NAME_1 + "\"\n" +
                "   target:\n" +
                "      stage: \"" + STAGE_NAME_2 + "\"\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> Verifications.verify(architectureInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "name"),
                exception.getMessage());
    }

    @Test
    public void missingMethodTypeTest() throws Exception {
        String content =
                "stages:\n" +
                "-  name: \"" + STAGE_NAME_1 + "\"\n" +
                "   host: " + HOST_1 + "\n" +
                "   port: " + PORT_1 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_1 + "\"\n" +
                "      type: UNARY\n" +
                "-  name: \"" + STAGE_NAME_2 + "\"\n" +
                "   host: " + HOST_2 + "\n" +
                "   port: " + PORT_2 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_2 + "\"\n" +
                "links:\n" +
                "-  source:\n" +
                "      stage: \"" + STAGE_NAME_1 + "\"\n" +
                "   target:\n" +
                "      stage: \"" + STAGE_NAME_2 + "\"\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> Verifications.verify(architectureInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "type"),
                exception.getMessage());
    }

    @Test
    public void missingLinkTargetTest() throws Exception {
        String content =
                "stages:\n" +
                "-  name: \"" + STAGE_NAME_1 + "\"\n" +
                "   host: " + HOST_1 + "\n" +
                "   port: " + PORT_1 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_1 + "\"\n" +
                "      type: UNARY\n" +
                "-  name: \"" + STAGE_NAME_2 + "\"\n" +
                "   host: " + HOST_2 + "\n" +
                "   port: " + PORT_2 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_2 + "\"\n" +
                "      type: UNARY\n" +
                "links:\n" +
                "-  source:\n" +
                "      stage: \"" + STAGE_NAME_1 + "\"\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> Verifications.verify(architectureInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "target"),
                exception.getMessage());
    }

    @Test
    public void missingLinkTargetStageTest() throws Exception {
        String content =
                "stages:\n" +
                "-  name: \"" + STAGE_NAME_1 + "\"\n" +
                "   host: " + HOST_1 + "\n" +
                "   port: " + PORT_1 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_1 + "\"\n" +
                "      type: UNARY\n" +
                "-  name: \"" + STAGE_NAME_2 + "\"\n" +
                "   host: " + HOST_2 + "\n" +
                "   port: " + PORT_2 + "\n" +
                "   method:\n" +
                "      name: \"" + METHOD_NAME_2 + "\"\n" +
                "      type: UNARY\n" +
                "links:\n" +
                "-  source:\n" +
                "      stage: \"" + STAGE_NAME_1 + "\"\n" +
                "   target:\n" +
                "      stage:\n";

        ArchitectureInformationDto architectureInformation = MAPPER.readValue(
                content,
                ArchitectureInformationDto.class);

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> Verifications.verify(architectureInformation));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "stage"),
                exception.getMessage());
    }
}
