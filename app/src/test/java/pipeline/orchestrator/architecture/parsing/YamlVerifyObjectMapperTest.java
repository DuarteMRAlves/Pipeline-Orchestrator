package pipeline.orchestrator.architecture.parsing;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import pipeline.orchestrator.verification.annotations.Verifiable;
import pipeline.orchestrator.verification.annotations.VerifyNotNull;
import pipeline.orchestrator.verification.annotations.VerifyPositive;
import pipeline.orchestrator.verification.exceptions.NotNullVerificationException;
import pipeline.orchestrator.verification.exceptions.PositiveVerificationException;

import static org.junit.Assert.*;

public class YamlVerifyObjectMapperTest {

    private static final String NORMAL_STRING_VALUE = "VALUE 1";
    private static final String NOT_NULL_STRING_VALUE = "VALUE 2";

    private static final int NORMAL_INT_VALUE = 1;
    private static final int POSITIVE_INT_VALUE = 2;

    private static final VerifyObjectMapper MAPPER = new VerifyObjectMapper(new YAMLFactory());

    @Test
    public void allFieldsCorrectTest() throws Exception{
        String content = "normalStringField: \"" + NORMAL_STRING_VALUE + "\"\n"
                + "notNullField: \"" + NOT_NULL_STRING_VALUE + "\"\n"
                + "normalIntField: " + NORMAL_INT_VALUE + "\n"
                + "positiveField: " + POSITIVE_INT_VALUE;

        TestDto result = MAPPER.readValue(content, TestDto.class);

        assertEquals(NORMAL_STRING_VALUE, result.getNormalStringField());
        assertEquals(NOT_NULL_STRING_VALUE, result.getNotNullField());
        assertEquals(NORMAL_INT_VALUE, result.getNormalIntField());
        assertEquals(POSITIVE_INT_VALUE, result.getPositiveField());
    }

    @Test
    public void nullNormalFieldTest() throws Exception{
        String content = "notNullField: \"" + NOT_NULL_STRING_VALUE + "\"\n"
                + "normalIntField: " + NORMAL_INT_VALUE + "\n"
                + "positiveField: " + POSITIVE_INT_VALUE;

        TestDto result = MAPPER.readValue(content, TestDto.class);

        assertNull(result.getNormalStringField());
        assertEquals(NOT_NULL_STRING_VALUE, result.getNotNullField());
        assertEquals(NORMAL_INT_VALUE, result.getNormalIntField());
        assertEquals(POSITIVE_INT_VALUE, result.getPositiveField());
    }

    @Test
    public void zeroNormalFieldTest() throws Exception{
        String content = "normalStringField: \"" + NORMAL_STRING_VALUE + "\"\n"
                + "notNullField: \"" + NOT_NULL_STRING_VALUE + "\"\n"
                + "positiveField: " + POSITIVE_INT_VALUE;

        TestDto result = MAPPER.readValue(content, TestDto.class);

        assertEquals(NORMAL_STRING_VALUE, result.getNormalStringField());
        assertEquals(NOT_NULL_STRING_VALUE, result.getNotNullField());
        assertEquals(0, result.getNormalIntField());
        assertEquals(POSITIVE_INT_VALUE, result.getPositiveField());
    }

    @Test
    public void nullNotNullFieldTest() {
        String content = "normalStringField: \""+ NORMAL_STRING_VALUE +"\"\n"
                + "normalIntField: "+ NORMAL_INT_VALUE +"\n"
                + "positiveField: " + POSITIVE_INT_VALUE;

        NotNullVerificationException exception = assertThrows(
                NotNullVerificationException.class,
                () -> MAPPER.readValue(content, TestDto.class));

        assertEquals(
                String.format(NotNullVerificationException.MESSAGE, "notNullField"),
                exception.getMessage());
    }

    @Test
    public void testNotPositiveField() {
        String content = "normalStringField: \""+ NORMAL_STRING_VALUE +"\"\n"
                + "notNullField: \"" + NOT_NULL_STRING_VALUE + "\"\n"
                + "normalIntField: "+ NORMAL_INT_VALUE;

        PositiveVerificationException exception = assertThrows(
                PositiveVerificationException.class,
                () -> MAPPER.readValue(content, TestDto.class));

        assertEquals(
                String.format(PositiveVerificationException.MESSAGE, "positiveField"),
                exception.getMessage());
    }

    @Verifiable
    public static class TestDto {

        private String normalStringField;

        @VerifyNotNull
        private String notNullField;

        private int normalIntField;

        @VerifyPositive
        private int positiveField;

        public String getNormalStringField() {
            return normalStringField;
        }

        public String getNotNullField() {
            return notNullField;
        }

        public int getNormalIntField() {
            return normalIntField;
        }

        public int getPositiveField() {
            return positiveField;
        }
    }
}
