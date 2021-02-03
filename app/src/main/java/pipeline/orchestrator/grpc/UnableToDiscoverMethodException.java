package pipeline.orchestrator.grpc;

public class UnableToDiscoverMethodException extends Exception {

    private static final String BASE_MSG = "Unable to discover method: ";

    private static final String UNKNOWN_ERROR_MSG = "Unknown error";

    public UnableToDiscoverMethodException() {
        super(String.join("", BASE_MSG, UNKNOWN_ERROR_MSG));
    }

    public UnableToDiscoverMethodException(String error) {
        super(String.join("", BASE_MSG, error));
    }

    public UnableToDiscoverMethodException(String error, Throwable cause) {
        super(String.join("", BASE_MSG, error), cause);
    }
}
