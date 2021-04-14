package pipeline.orchestrator.architecture.parsing;

import com.google.common.graph.ImmutableValueGraph;
import org.junit.Test;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Test for the following architecture
 * 1 > > 2 >
 *           > 4
 *       3 >
 */
public class YamlMergeArchitectureTest {

    private static final String NAME_1 = "Stage 1";
    private static final String NAME_2 = "Stage 2";
    private static final String NAME_3 = "Stage 3";
    private static final String NAME_4 = "Stage 4";

    private static final Set<String> STAGES = Set.of(NAME_1,
                                                     NAME_2,
                                                     NAME_3,
                                                     NAME_4);

    private static final String CONFIG_FILE =
            "src/test/resources/yaml/merge_architecture.yaml";

    @Test
    public void mergeArchitectureTest() throws Exception {
        ImmutableValueGraph<StageInformation, LinkInformation> graph =
                ArchitectureParser.parseYaml(CONFIG_FILE)
                        .getArchitecture();

        Set<StageInformation> nodes = graph.nodes();
        assertEquals(4, nodes.size());
        for (StageInformation info : nodes) {
            assertTrue(STAGES.contains(info.getName()));
            switch (info.getName()) {
                case NAME_1:
                    assertNode1(graph, info);
                    break;
                case NAME_2:
                    assertNode2(graph, info);
                    break;
                case NAME_3:
                    assertNode3(graph, info);
                    break;
                case NAME_4:
                    assertNode4(graph, info);
                    break;
                default:
                    throw new RuntimeException("Unknown Stage");
            }
        }
        Set<LinkInformation> links = graph.edges().stream()
                .map(edge -> graph.edgeValue(edge))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        assertLinks(links);
    }

    private void assertNode1(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage
    ) {
        assertEquals(NAME_1, stage.getName());
        assertEquals("Host1", stage.getServiceHost());
        assertEquals(1, stage.getServicePort());
        assertTrue(stage.getServiceName().isEmpty());
        assertTrue(stage.getMethodName().isPresent());
        assertEquals("Method 1", stage.getMethodName().get());

        Set<StageInformation> succStages = graph.successors(stage);
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(1, succStages.size());
        assertEquals(NAME_2, succStages.iterator().next().getName());
        assertEquals(0, predStages.size());
    }

    private void assertNode2(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage
    ) {

        assertEquals(NAME_2, stage.getName());
        assertEquals("Host2", stage.getServiceHost());
        assertEquals(2, stage.getServicePort());
        assertTrue(stage.getServiceName().isEmpty());
        assertTrue(stage.getMethodName().isPresent());
        assertEquals("Method 2", stage.getMethodName().get());

        Set<StageInformation> succStages = graph.successors(stage);
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(1, succStages.size());
        assertEquals(NAME_4, succStages.iterator().next().getName());
        assertEquals(1, predStages.size());
        assertEquals(NAME_1, predStages.iterator().next().getName());
    }

    private void assertNode3(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage
    ) {

        assertEquals(NAME_3, stage.getName());
        assertEquals("Host3", stage.getServiceHost());
        assertEquals(3, stage.getServicePort());
        assertTrue(stage.getServiceName().isEmpty());
        assertTrue(stage.getMethodName().isEmpty());

        Set<StageInformation> succStages = graph.successors(stage);
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(1, succStages.size());
        assertEquals(NAME_4, succStages.iterator().next().getName());
        assertEquals(0, predStages.size());
    }

    private void assertNode4(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage
    ) {

        assertEquals(NAME_4, stage.getName());
        assertEquals("Host4", stage.getServiceHost());
        assertEquals(4, stage.getServicePort());
        assertTrue(stage.getServiceName().isEmpty());
        assertTrue(stage.getMethodName().isEmpty());

        Set<StageInformation> succStages = graph.successors(stage);
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(0, succStages.size());
        assertEquals(2, predStages.size());
        Set<String> predNames = Set.of(NAME_2, NAME_3);
        predStages.forEach(predStage -> {
            assertTrue(predNames.contains(predStage.getName()));
        });
    }

    private void assertLinks(Set<LinkInformation> links) {
        for (LinkInformation link : links) {
            String sourceStage = link.getSourceStageName();
            String targetStage = link.getTargetStageName();
            if (sourceStage.equals(NAME_1) && targetStage.equals(NAME_2)) {
                assertTrue(link.getSourceFieldName().isEmpty());
                assertTrue(link.getTargetFieldName().isEmpty());
            } else if (sourceStage.equals(NAME_2)
                    && targetStage.equals(NAME_4)) {
                assertTrue(link.getSourceFieldName().isPresent());
                assertEquals("Field 2", link.getSourceFieldName().get());
                assertTrue(link.getTargetFieldName().isPresent());
                assertEquals("Field 2-4", link.getTargetFieldName().get());
            } else if (sourceStage.equals(NAME_3)
                    && targetStage.equals(NAME_4)) {
                assertTrue(link.getSourceFieldName().isEmpty());
                assertTrue(link.getTargetFieldName().isPresent());
                assertEquals("Field 3-4", link.getTargetFieldName().get());
            } else {
                fail(String.format("Unknown link '%s'->'%s'",
                                   sourceStage,
                                   targetStage));
            }
        }
    }
}
