package pipeline.orchestrator.grpc;

public class FailedToExecuteRequestException extends Exception {

    public FailedToExecuteRequestException(String message) {
        super(message);
    }

    public FailedToExecuteRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
