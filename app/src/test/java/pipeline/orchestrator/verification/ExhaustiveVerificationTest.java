package pipeline.orchestrator.verification;

import org.junit.Test;
import pipeline.orchestrator.verification.annotations.Verifiable;
import pipeline.orchestrator.verification.annotations.VerifyNotNull;
import pipeline.orchestrator.verification.annotations.VerifyPositive;
import pipeline.orchestrator.verification.errors.ErrorReport;
import pipeline.orchestrator.verification.errors.NotNullVerificationError;
import pipeline.orchestrator.verification.errors.PositiveVerificationError;
import pipeline.orchestrator.verification.errors.VerificationError;

import java.util.List;

import static org.junit.Assert.*;

public class ExhaustiveVerificationTest {

    @Test
    public void noErrorsTest() {
        TestClass object = buildTestObject("Not Null", 1);

        ErrorReport errorReport = Verifications.exhaustiveVerification(object);

        assertFalse(errorReport.hasErrors());
        assertTrue(errorReport.getErrors().isEmpty());
    }

    @Test
    public void nullErrorTest() {
        TestClass object = buildTestObject(null, 1);

        ErrorReport errorReport = Verifications.exhaustiveVerification(object);

        assertTrue(errorReport.hasErrors());

        List<VerificationError> errors = errorReport.getErrors();
        assertEquals(1, errors.size());

        VerificationError error = errors.get(0);
        assertTrue(error instanceof NotNullVerificationError);
        assertEquals("notNullField", ((NotNullVerificationError) error).getField());
    }

    @Test
    public void zeroErrorTest() {
        nonPositiveTest(0);
    }

    @Test
    public void negativeErrorTest() {
        nonPositiveTest(-1);
    }

    private void nonPositiveTest(int testVal) {
        TestClass object = buildTestObject("Not Null", testVal);

        ErrorReport errorReport = Verifications.exhaustiveVerification(object);

        assertTrue(errorReport.hasErrors());

        List<VerificationError> errors = errorReport.getErrors();
        assertEquals(1, errors.size());

        VerificationError error = errors.get(0);
        assertTrue(error instanceof PositiveVerificationError);
        assertEquals("positiveField", ((PositiveVerificationError) error).getField());
        assertEquals(testVal, ((PositiveVerificationError) error).getValue());
    }

    @Test
    public void multipleErrorsTest() {
        TestClass object = buildTestObject(null, 0);

        ErrorReport errorReport = Verifications.exhaustiveVerification(object);

        assertTrue(errorReport.hasErrors());

        List<VerificationError> errors = errorReport.getErrors();
        assertEquals(2, errors.size());

        errors.forEach(error -> assertTrue(
                checkPossibleNullError(error) || checkPossiblePositiveError(error, 0)));
    }

    private boolean checkPossibleNullError(VerificationError error) {
        if (error instanceof NotNullVerificationError) {
            assertEquals("notNullField", ((NotNullVerificationError) error).getField());
            return true;
        }
        return false;
    }

    private boolean checkPossiblePositiveError(VerificationError error, int testVal) {
        if (error instanceof PositiveVerificationError) {
            assertEquals("positiveField", ((PositiveVerificationError) error).getField());
            assertEquals(testVal, ((PositiveVerificationError) error).getValue());
            return true;
        }
        return false;
    }

    private TestClass buildTestObject(String notNullField, int positiveField) {
        TestClass object = new TestClass();
        object.notNullField = notNullField;
        object.positiveField = positiveField;
        return object;
    }

    /**
     * Dummy class to test verifications
     */
    @Verifiable
    private static class TestClass {

        private String notVerifiedString;

        private int notVerifiedInt;

        @VerifyNotNull
        private String notNullField;

        @VerifyPositive
        private int positiveField;
    }
}
