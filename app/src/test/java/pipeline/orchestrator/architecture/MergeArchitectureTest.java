package pipeline.orchestrator.architecture;

import com.google.common.collect.Lists;
import com.google.common.graph.ImmutableValueGraph;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test for the following architecture
 * 1 > > 2 >
 *           > 4
 *       3 >
 */
public class MergeArchitectureTest {

    private static final String NODES_FILE = "src/test/resources/service_stages.csv";
    private static final String EDGES_FILE = "src/test/resources/merge_architecture.csv";

    private static final String HOST_1 = "HOST_1";
    private static final String HOST_2 = "HOST_2";
    private static final String HOST_3 = "HOST_3";
    private static final String HOST_4 = "HOST_4";

    private static final int PORT_1 = 1;
    private static final int PORT_2 = 2;
    private static final int PORT_3 = 3;
    private static final int PORT_4 = 4;

    private static final String VAR_2 = "VAR_2";
    private static final String VAR_3 = "VAR_3";

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
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(1, succStages.size());
        assertEquals(HOST_4, succStages.iterator().next().getServiceHost());
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
        assertEquals(1, succStages.size());
        assertEquals(HOST_4, succStages.iterator().next().getServiceHost());
        assertEquals(0, predStages.size());
    }

    private void assertNode4(
            ImmutableValueGraph<StageInformation, LinkInformation> graph,
            StageInformation stage) {
        assertEquals(HOST_4, stage.getServiceHost());
        assertEquals(PORT_4, stage.getServicePort());

        Set<StageInformation> succStages = graph.successors(stage);
        Set<StageInformation> predStages = graph.predecessors(stage);
        assertEquals(0, succStages.size());
        assertEquals(2, predStages.size());

        List<StageInformation> predStagesList = Lists.newArrayList(predStages.iterator());
        assertEquals(2, predStagesList.size());

        int stageTwoIndex = -1;
        int stageThreeIndex = -1;
        for (int i = 0; i < predStagesList.size(); i++) {
            if (HOST_2.equals(predStagesList.get(i).getServiceHost())) {
                stageTwoIndex = i;
            }
            else if (HOST_3.equals(predStagesList.get(i).getServiceHost())) {
                stageThreeIndex = i;
            }
        }
        assertNotEquals(-1, stageTwoIndex);
        assertNotEquals(-1, stageThreeIndex);

        LinkInformation linkInformation = graph.edgeValue(predStagesList.get(stageTwoIndex), stage)
                .orElse(null);

        assertNotNull(linkInformation);
        assertEquals(VAR_2, linkInformation.getTargetFieldName());

        linkInformation = graph.edgeValue(predStagesList.get(stageThreeIndex), stage)
                .orElse(null);

        assertNotNull(linkInformation);
        assertEquals(VAR_3, linkInformation.getTargetFieldName());
    }
}
