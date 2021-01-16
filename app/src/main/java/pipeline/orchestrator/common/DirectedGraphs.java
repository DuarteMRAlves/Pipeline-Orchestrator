package pipeline.orchestrator.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;

/**
 * Helper static class for managing directed graphs
 * using the guava interface
 */
public class DirectedGraphs {

    private DirectedGraphs() {}

    public static boolean isDirectedGraph(Graph<?> graph) {
        Preconditions.checkNotNull(graph);
        return !Graphs.hasCycle(graph);
    }

    public static <T> ImmutableList<T> reversedTopologicalOrder(Graph<T> graph) {
        Preconditions.checkArgument(isDirectedGraph(graph));
        return new ReversedTopologicalSort<T>().apply(graph);
    }
}
