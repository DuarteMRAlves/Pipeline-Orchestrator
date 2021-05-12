package pipeline.orchestrator.execution;

@FunctionalInterface
public interface LinkListener {

    void onNewObject(Link link);
}
