package pipeline.orchestrator.protobuf;

public class CircularDependenciesException extends IllegalArgumentException {

    private static final String MESSAGE = "Circular dependencies found in file set";

    public CircularDependenciesException() {
        super(MESSAGE);
    }
}
