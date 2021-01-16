package pipeline.orchestrator.common;

import com.google.common.graph.Graph;

/**
 * Class to represent an algorithm that can be applied to a graph
 * @param <N> type of the graph nodes
 * @param <T> the return type of the algorithm
 */
public interface GraphAlgorithm<N, T> {

    T apply(Graph<N> graph);
}
