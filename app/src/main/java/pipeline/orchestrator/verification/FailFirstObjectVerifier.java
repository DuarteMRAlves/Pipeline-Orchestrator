package pipeline.orchestrator.verification;

import pipeline.orchestrator.verification.exceptions.NotNullVerificationException;
import pipeline.orchestrator.verification.exceptions.PositiveVerificationException;

/**
 * Class to verify predicates about the values of a given object
 * This class fails the first time an error is encountered with
 * an exception
 */
public class FailFirstObjectVerifier extends AbstractObjectVerifier {

    /**
     * Package private
     * Accesses should be made through the {@link Verifications} API
     */
    FailFirstObjectVerifier() {}

    public void verify(Object object) {
        verifyTemplate(object);
    }

    @Override
    protected void setContext() {
        // No need to initialize context
    }

    @Override
    protected void onNonPositiveField(String name, int value) {
        throw new PositiveVerificationException(name);
    }

    @Override
    protected void onNullField(String name) {
        throw new NotNullVerificationException(name);
    }
}
