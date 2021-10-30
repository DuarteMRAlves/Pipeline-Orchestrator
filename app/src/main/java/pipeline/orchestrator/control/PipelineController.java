package pipeline.orchestrator.control;

import com.google.common.base.Preconditions;
import com.google.common.graph.ValueGraph;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.ExecutionOrchestrator;

/**
 * Main class to control functioning of the pipeline
 */
public class PipelineController {

    private ValueGraph<StageInformation, LinkInformation> architecture;
    private ExecutionOrchestrator orchestrator;

    public synchronized void updateGraph(
            ValueGraph<StageInformation, LinkInformation> architecture
    ) {
        Preconditions.checkNotNull(architecture);
        this.architecture = architecture;
    }

    public synchronized void start() {
        if (orchestrator != null) orchestrator.finish();
        orchestrator = new ExecutionOrchestrator(architecture);
        orchestrator.run();
    }

    public synchronized void finish() {
        Preconditions.checkState(orchestrator != null);
        orchestrator.finish();
    }
}
