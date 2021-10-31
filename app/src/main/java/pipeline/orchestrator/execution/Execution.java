package pipeline.orchestrator.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.execution.stages.ExecutionStage;
import pipeline.orchestrator.execution.stages.ExecutionStages;

import java.util.Iterator;
import java.util.Set;

/**
 * Orchestrator class responsible for the entire execution of the pipeline
 */
public class Execution implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(Execution.class);

    private boolean running = false;
    private final ImmutableMap<String, ExecutionStage> executionStages;

    public Execution(
            ValueGraph<StageInformation, LinkInformation> architecture) {

        Preconditions.checkNotNull(architecture);

        // Create pipeline stages
        Iterator<ExecutionStage> stages = architecture.nodes().stream()
                .map(ExecutionStages::buildStage)
                .iterator();
        this.executionStages = Maps.uniqueIndex(
                stages,
                ExecutionStage::getName);

        // Create links
        Set<EndpointPair<StageInformation>> endpoints = architecture.edges();
        for (EndpointPair<StageInformation> endpoint : endpoints) {

            LinkInformation linkInformation = architecture.edgeValue(endpoint)
                    .orElseThrow(IllegalArgumentException::new);

            ExecutionStage sourceStage = executionStages.get(endpoint.source().getName());
            ExecutionStage targetStage = executionStages.get(endpoint.target().getName());

            ExecutionStages.linkStages(
                    sourceStage,
                    targetStage,
                    linkInformation);
        }
    }

    @Override
    public void run() {
        LOGGER.info("Starting Pipeline Execution");
        executionStages.values().forEach(
                executionStage -> new Thread(executionStage).start());
        setRunning(true);
    }

    public void pause() {
        if (isRunning()) {
            executionStages.values().forEach(ExecutionStage::pause);
        }
    }

    public void resume() {
        if (isRunning()) {
            executionStages.values().forEach(ExecutionStage::resume);
        }
    }

    public void finish() {
        if (isRunning()) {
            executionStages.values().forEach(ExecutionStage::finish);
            setRunning(false);
        }
    }

    private synchronized boolean isRunning() {
        return running;
    }

    private synchronized void setRunning(boolean running) {
        this.running = running;
    }
}
