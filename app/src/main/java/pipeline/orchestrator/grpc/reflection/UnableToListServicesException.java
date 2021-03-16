package pipeline.orchestrator.grpc.reflection;

import io.grpc.StatusRuntimeException;
import pipeline.orchestrator.grpc.utils.StatusRuntimeExceptions;

/**
 * Exception to signal that an error happened when
 * trying to list the available services in
 * a grpc server
 */
public class UnableToListServicesException extends Exception {

    private static final String UNAVAILABLE_SERVICE_MSG = "Unable to list services: Server unavailable.";
    private static final String NO_REFLECTION_MSG = "Unable to list services: Server does not have reflection enabled.";
    private static final String UNKNOWN_ERROR_MSG = "Unable to list services: Unknown error.";

    private String message = null;

    public UnableToListServicesException() {
        super(UNKNOWN_ERROR_MSG);
        message = UNKNOWN_ERROR_MSG;
    }

    public UnableToListServicesException(Throwable cause) {
        super(cause);
        setMessageFromCause(cause);
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        super.initCause(cause);
        setMessageFromCause(cause);
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    private void setMessageFromCause(Throwable cause) {
        // Common cause for errors
        if (cause instanceof StatusRuntimeException) {
            setMessageFromStatusRuntimeException((StatusRuntimeException) cause);
        }
        else {
            message = UNKNOWN_ERROR_MSG;
        }
    }

    private void setMessageFromStatusRuntimeException(
            StatusRuntimeException exception) {

        if (StatusRuntimeExceptions.isUnavailable(exception)) {
            message = UNAVAILABLE_SERVICE_MSG;
        }
        else if (StatusRuntimeExceptions.isUnimplemented(exception)) {
            message = NO_REFLECTION_MSG;
        }
        else {
            message = UNKNOWN_ERROR_MSG;
        }
    }
}

