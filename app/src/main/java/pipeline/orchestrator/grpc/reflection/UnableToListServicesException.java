package pipeline.orchestrator.grpc.reflection;

/**
 * Exception to signal that an error happened when
 * trying to list the available services in
 * a grpc server
 */
public class UnableToListServicesException extends Exception {

    private static final String UNABLE_TO_LIST_SERVICES_ERROR_MSG = "Unable to list services";

    public UnableToListServicesException() {
        super(UNABLE_TO_LIST_SERVICES_ERROR_MSG);
    }

    public UnableToListServicesException(Throwable cause) {
        super(UNABLE_TO_LIST_SERVICES_ERROR_MSG, cause);
    }
}

