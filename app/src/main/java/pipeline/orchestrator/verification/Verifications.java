package pipeline.orchestrator.verification;

import pipeline.orchestrator.verification.errors.ErrorReport;
import pipeline.orchestrator.verification.exceptions.VerificationException;

/**
 * Class to offer verifications to objects attributes
 * according to annotations regarding predicates
 */
public class Verifications {

    private Verifications() {}

    /**
     * Verifies if the object fields verify the given annotations
     * restrictions. It fails on the first error encountered
     * throwing the respective exception
     * @param object object to verify
     * @throws VerificationException if a field with an error is detected
     */
    public static void verify(Object object) {
        new FailFirstObjectVerifier().verify(object);
    }

    /**
     * Verifies if the object fields verify the given annotations
     * restrictions. This method does not stop on the first error
     * encountered, collection all found violations.
     * @param object object to verify.
     * @return error report with all the errors.
     */
    public static ErrorReport exhaustiveVerification(Object object) {
        return new ExhaustiveObjectVerifier().verify(object);
    }
}
