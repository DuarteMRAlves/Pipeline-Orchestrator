package pipeline.orchestrator.protobuf;

public class MissingDependencyException extends IllegalArgumentException {

    private static final String MESSAGE = "Missing dependency '%s'";

    public MissingDependencyException(String dependency) {
        super(String.format(MESSAGE, dependency));
    }
}
