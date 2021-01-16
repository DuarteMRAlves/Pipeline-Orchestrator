package pipeline.orchestrator.grpc.reflection;

/**
 * Exception to signal the unexpected termination of
 * a stream of reflection requests.
 * Happens when some requests are left unanswered
 */
public class ReflectionStreamUnexpectedCompletionException extends Exception {

    public ReflectionStreamUnexpectedCompletionException(String message, Object... params) {
        super(String.format(message, params));
    }
}
