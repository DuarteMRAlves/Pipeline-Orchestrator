package pipeline.orchestrator.architecture.parsing;

import com.google.common.collect.Streams;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to build the architecture graph form the information dto
 */
public class ArchitectureGraphBuilder {

    private ArchitectureGraphBuilder() {}

    public static ImmutableValueGraph<StageInformation, LinkInformation> build(
            ArchitectureInformationDto architectureInformation) {

        Map<String, StageInformation> stages = buildStages(architectureInformation.getStages());
        Set<LinkInformation> links = buildLinks(architectureInformation.getLinks());
        return buildGraph(stages, links);
    }

    private static Map<String, StageInformation> buildStages(Iterable<StageInformationDto> stages) {
        return Streams.stream(stages)
                .map(ArchitectureGraphBuilder::buildStage)
                .map(stage -> Map.entry(stage.getName(), stage))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static StageInformation buildStage(StageInformationDto dto) {
        return StageInformation.newBuilder()
                .setName(dto.getName())
                .setServiceHost(dto.getHost())
                .setServicePort(dto.getPort())
                .setMethodName(dto.getMethod().getName())
                .setMethodType(dto.getMethod().getType())
                .build();
    }

    private static Set<LinkInformation> buildLinks(Iterable<LinkInformationDto> links) {
        return Streams.stream(links)
                .map(ArchitectureGraphBuilder::buildLink)
                .collect(Collectors.toSet());
    }

    private static LinkInformation buildLink(LinkInformationDto dto) {
        return LinkInformation.newBuilder()
                .setSourceStageName(dto.getSource().getStage())
                .setTargetStageName(dto.getTarget().getStage())
                .setSourceFieldName(dto.getSource().getField())
                .setTargetFieldName(dto.getTarget().getField())
                .build();
    }

    private static ImmutableValueGraph<StageInformation, LinkInformation> buildGraph(
            Map<String, StageInformation> stages,
            Set<LinkInformation> links) {

        MutableValueGraph<StageInformation, LinkInformation> graph =
                ValueGraphBuilder.directed().build();

        for (LinkInformation linkInformation : links) {
            String sourceStage = linkInformation.getSourceStageName();
            String targetStage = linkInformation.getTargetStageName();
            graph.putEdgeValue(
                    stages.get(sourceStage),
                    stages.get(targetStage),
                    linkInformation);
        }

        return ImmutableValueGraph.copyOf(graph);
    }
}
