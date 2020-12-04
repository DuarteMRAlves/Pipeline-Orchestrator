package pipeline.orchestrator.architecture;

import com.google.common.collect.Lists;
import com.google.common.graph.ImmutableValueGraph;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Test for the following architecture
 *           > 3
 * 1 > > 2 >
 *           > 4
 */
public class SplitArchitectureTest {

    private static final String NODES_FILE = "src/test/resources/service_stages.csv";
    private static final String EDGES_FILE = "src/test/resources/split_architecture.csv";

    private static final String HOST_1 = "HOST_1";
    private static final String HOST_2 = "HOST_2";
    private static final String HOST_3 = "HOST_3";
    private static final String HOST_4 = "HOST_4";

    private static final int PORT_1 = 1;
    private static final int PORT_2 = 2;
    private static final int PORT_3 = 3;
    private static final int PORT_4 = 4;

    private static final String VAR_3 = "VAR_3";
    private static final String VAR_4 = "VAR_4";

    @Test
    public void testSingleLinePipeline() throws Exception {
        ImmutableValueGraph<StageInformation, LinkInformation> stageInformationGraph = ArchitectureParser.parse(
                NODES_FILE,
                EDGES_FILE);

        Set<StageInformation> nodes = stageInformationGraph.nodes();
        assertEquals(4, nodes.size());
        for (StageInformation info : nodes) {
            assertTrue(List.of(HOST_1, HOST_2, HOST_3, HOST_4).contains(info.getServiceHost()));
            switch (info.getServiceHost()) {
                case HOST_1:
                    assertNode1(stageInformationGraph, info);
                    break;
                case HOST_2:
                    assertNode2(stageInformationGraph, info);
                    break;
                case HOST_3:
                    assertNode3(stageInformationGraph, info);
                    break;
                case HOST_4:
                    assertNode4(stageInformationGraph, info);
                    break;
                default:
                    throw new RuntimeException("Unknown Host");
            }
        }
    }


    private void assertNode1(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage) {
        assertEquals(HOST_1, stage.getServiceHost());
        assertEquals(PORT_1, stage.getServicePort());

        Set<StageInformation> succStages = graph.successors(stage);
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(1, succStages.size());
        assertEquals(HOST_2, succStages.iterator().next().getServiceHost());
        assertEquals(0, predStages.size());
    }

    private void assertNode2(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage) {
        assertEquals(HOST_2, stage.getServiceHost());
        assertEquals(PORT_2, stage.getServicePort());

        Set<StageInformation> succStages = graph.successors(stage);
        assertEquals(2, succStages.size());

        List<StageInformation> succStagesList = Lists.newArrayList(succStages);
        int stageThreeIndex = -1;
        int stageFourIndex = -1;
        for (int i = 0; i < succStagesList.size(); i++) {
            if (HOST_3.equals(succStagesList.get(i).getServiceHost())) {
                stageThreeIndex = i;
            }
            else if (HOST_4.equals(succStagesList.get(i).getServiceHost())) {
                stageFourIndex = i;
            }
        }
        assertNotEquals(-1, stageThreeIndex);
        assertNotEquals(-1, stageFourIndex);

        LinkInformation linkInformation = graph.edgeValue(
                stage,
                succStagesList.get(stageThreeIndex))
                .orElse(null);

        assertNotNull(linkInformation);
        assertEquals(VAR_3, linkInformation.getSourceFieldName());

        linkInformation = graph.edgeValue(stage, succStagesList.get(stageFourIndex))
                .orElse(null);

        assertNotNull(linkInformation);
        assertEquals(VAR_4, linkInformation.getSourceFieldName());

        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(1, predStages.size());
        assertEquals(HOST_1, predStages.iterator().next().getServiceHost());
    }

    private void assertNode3(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage) {
        assertEquals(HOST_3, stage.getServiceHost());
        assertEquals(PORT_3, stage.getServicePort());

        Set<StageInformation> succStages = graph.successors(stage);
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(0, succStages.size());
        assertEquals(1, predStages.size());
        assertEquals(HOST_2, predStages.iterator().next().getServiceHost());
    }

    private void assertNode4(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage) {
        assertEquals(HOST_4, stage.getServiceHost());
        assertEquals(PORT_4, stage.getServicePort());

        Set<StageInformation> succStages = graph.successors(stage);
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(0, succStages.size());
        assertEquals(1, predStages.size());
        assertEquals(HOST_2, predStages.iterator().next().getServiceHost());
    }
}
