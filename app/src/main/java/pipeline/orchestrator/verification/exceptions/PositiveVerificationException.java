package pipeline.orchestrator.verification.exceptions;

import com.google.common.annotations.VisibleForTesting;

/**
 * Exception to be thrown when a non positive field is
 * detected
 */
public class PositiveVerificationException extends VerificationException {

    @VisibleForTesting
    public static final String MESSAGE = "Field %s should be positive.";

    public PositiveVerificationException(String fieldName) {
        super(String.format(MESSAGE, fieldName));
    }

    public PositiveVerificationException(String fieldName, Throwable cause) {
        super(String.format(MESSAGE, fieldName), cause);
    }
}
