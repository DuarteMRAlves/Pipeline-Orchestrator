package pipeline.orchestrator.verification;

import pipeline.orchestrator.verification.exceptions.VerificationException;

/**
 * Class to offer verifications to objects attributes
 * according to annotations regarding predicates
 */
public class Verifications {

    private Verifications() {}

    /**
     * Verifies if the object fields verify the given annotations
     * restrictions
     * @param object object to verify
     * @throws VerificationException if a field with an error is detected
     */
    public static void verify(Object object) {
        new ObjectVerifier().verify(object);
    }
}
