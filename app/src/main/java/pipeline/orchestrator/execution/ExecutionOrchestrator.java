package pipeline.orchestrator.execution;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;

import java.util.Map;
import java.util.Set;

/**
 * Orchestrator class responsible for the entire execution of the pipeline
 */
public class ExecutionOrchestrator implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ExecutionOrchestrator.class);

    private final Map<StageInformation, PipelineStage> executionStages;

    public ExecutionOrchestrator(
            ValueGraph<StageInformation, LinkInformation> architecture) {

        Preconditions.checkNotNull(architecture);

        // Create pipeline stages
        Set<StageInformation> stages = architecture.nodes();
        this.executionStages = Maps.toMap(stages, PipelineStage::new);

        // Create links
        Set<EndpointPair<StageInformation>> endpoints = architecture.edges();
        for (EndpointPair<StageInformation> endpoint : endpoints) {

            LinkInformation linkInformation = architecture.edgeValue(endpoint)
                    .orElseThrow(IllegalArgumentException::new);

            PipelineStage sourceStage = executionStages.get(endpoint.source());
            PipelineStage targetStage = executionStages.get(endpoint.target());

            LOGGER.info(
                    "Linking stages {}, {} with information {}",
                    endpoint.source(),
                    endpoint.target(),
                    linkInformation);

            PipelineStage.linkStages(
                    sourceStage,
                    targetStage,
                    linkInformation);
        }
    }

    @Override
    public void run() {
        this.executionStages.values().forEach(pipelineStage -> new Thread(pipelineStage).start());
    }
}
