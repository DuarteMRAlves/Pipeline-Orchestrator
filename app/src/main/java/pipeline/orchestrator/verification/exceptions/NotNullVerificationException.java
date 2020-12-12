package pipeline.orchestrator.verification.exceptions;

import com.google.common.annotations.VisibleForTesting;

/**
 * Exception to be thrown when a null field is
 * detected
 */
public class NotNullVerificationException extends VerificationException {

    @VisibleForTesting
    public static final String MESSAGE = "Field %s should not be null.";

    public NotNullVerificationException(String fieldName) {
        super(String.format(MESSAGE, fieldName));
    }

    public NotNullVerificationException(String fieldName, Throwable cause) {
        super(String.format(MESSAGE, fieldName), cause);
    }
}
