package pipeline.orchestrator.grpc.reflection;

/**
 * Exception that signals that an error happened
 * when trying to lookup a service
 */
public class UnableToLookupServiceException extends Exception {

    private static final String UNABLE_TO_LOOKUP_SERVICE_ERROR_MSG = "Unable to lookup service";

    public UnableToLookupServiceException() {
        super(UNABLE_TO_LOOKUP_SERVICE_ERROR_MSG);
    }

    public UnableToLookupServiceException(Throwable cause) {
        super(UNABLE_TO_LOOKUP_SERVICE_ERROR_MSG, cause);
    }
}
