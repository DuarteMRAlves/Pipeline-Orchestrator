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
import pipeline.orchestrator.execution.stages.AbstractPipelineStage;
import pipeline.orchestrator.execution.stages.PipelineStages;

import java.util.Iterator;
import java.util.Set;

/**
 * Orchestrator class responsible for the entire execution of the pipeline
 */
public class ExecutionOrchestrator implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ExecutionOrchestrator.class);

    private final ImmutableMap<String, AbstractPipelineStage> executionStages;

    public ExecutionOrchestrator(
            ValueGraph<StageInformation, LinkInformation> architecture) {

        Preconditions.checkNotNull(architecture);

        // Create pipeline stages
        Iterator<AbstractPipelineStage> stages = architecture.nodes().stream()
                .map(PipelineStages::buildStage)
                .iterator();
        this.executionStages = Maps.uniqueIndex(
                stages,
                AbstractPipelineStage::getName);

        // Create links
        Set<EndpointPair<StageInformation>> endpoints = architecture.edges();
        for (EndpointPair<StageInformation> endpoint : endpoints) {

            LinkInformation linkInformation = architecture.edgeValue(endpoint)
                    .orElseThrow(IllegalArgumentException::new);

            AbstractPipelineStage sourceStage = executionStages.get(endpoint.source().getName());
            AbstractPipelineStage targetStage = executionStages.get(endpoint.target().getName());

            LOGGER.info(
                    "Linking stages {}, {} with information {}",
                    endpoint.source(),
                    endpoint.target(),
                    linkInformation);

            PipelineStages.linkStages(
                    sourceStage,
                    targetStage,
                    linkInformation);
        }
    }

    @Override
    public void run() {
        new StagesMonitor(executionStages);
        executionStages.values().forEach(pipelineStage -> new Thread(pipelineStage).start());
    }
}
