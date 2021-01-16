package pipeline.orchestrator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReversedTopologicalSortTest {

    private static final ReversedTopologicalSort<Integer> REVERSED_TOPOLOGICAL_SORT =
            new ReversedTopologicalSort<>();

    /**
     * Graph Adj List:
     * 1 -> 2
     * 2 -> 3
     * 3 -> 4
     */
    @Test
    public void testSingleLineGraph() {
        ImmutableGraph<Integer> graph = GraphBuilder.directed()
                .<Integer>immutable()
                .putEdge(3, 4)
                .putEdge(2, 3)
                .putEdge(1, 2)
                .build();

        ImmutableList<Integer> result = REVERSED_TOPOLOGICAL_SORT.apply(graph);

        // Result must be 4, 3, 2, 1
        assertEquals(4, result.size());
        assertEquals(4, (int) result.get(0));
        assertEquals(3, (int) result.get(1));
        assertEquals(2, (int) result.get(2));
        assertEquals(1, (int) result.get(3));
    }

    /**
     * Graph Adj List:
     * 1 -> 2, 3
     * 2 -> 4
     * 3 -> 4
     */
    @Test
    public void testNonSingleLineGraph() {
        ImmutableGraph<Integer> graph = GraphBuilder.directed()
                .<Integer>immutable()
                .putEdge(2, 4)
                .putEdge(1, 2)
                .putEdge(1, 3)
                .putEdge(3, 4)
                .build();

        ImmutableList<Integer> result = REVERSED_TOPOLOGICAL_SORT.apply(graph);

        // Result can be 4, 3, 2, 1 or 4, 2, 3, 1
        assertEquals(4, result.size());
        assertEquals(4, (int) result.get(0));
        assertTrue(result.get(1) == 3 || result.get(1) == 2);
        assertTrue(result.get(2) == 3 || result.get(2) == 2);
        assertNotEquals(result.get(3), result.get(2));
        assertEquals(1, (int) result.get(3));
    }
}
