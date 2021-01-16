package pipeline.orchestrator.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Static helper class to handle grpc StatusRuntimeExceptions
 */
public class StatusRuntimeExceptions {

    private StatusRuntimeExceptions() {}

    /**
     * Checks if the given exception was thrown due to
     * an unavailable endpoint
     * @param exception exception that was delivered
     * @return true if the exception means other endpoint of the
     *         channel is unavailable and false otherwise
     */
    public static boolean isUnavailable(StatusRuntimeException exception) {
        return exception.getStatus().getCode() == Status.Code.UNAVAILABLE;
    }
}
