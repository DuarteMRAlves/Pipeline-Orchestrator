package pipeline.orchestrator.verification.exceptions;

/**
 * Exception to be thrown when an error is
 * detected while verifying an object
 */
public class VerificationException extends RuntimeException {

    public VerificationException(String message) {
        super(message);
    }

    public VerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
