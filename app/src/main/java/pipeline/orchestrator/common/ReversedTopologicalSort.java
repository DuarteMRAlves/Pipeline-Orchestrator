package pipeline.orchestrator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to build the reversed topological order for a graph
 * @param <T> type of the graph nodes
 */
public class ReversedTopologicalSort<T> implements GraphAlgorithm<T, ImmutableList<T>> {

    private Graph<T> graph;
    private Set<T> nodes;
    private final Set<T> visited = new HashSet<>();
    private final List<T> reversedNodes = new ArrayList<>();

    @Override
    public ImmutableList<T> apply(Graph<T> graph) {
        this.graph = graph;
        nodes = graph.nodes();
        visited.clear();
        reversedNodes.clear();

        for (T node : nodes) {
            if (!visited.contains(node)) {
                visitNode(node);
            }
        }
        return ImmutableList.copyOf(reversedNodes);
    }

    private void visitNode(T current) {
        visited.add(current);
        for (T adjacent : graph.successors(current)) {
            if (!visited.contains(adjacent)) {
                visitNode(adjacent);
            }
        }
        reversedNodes.add(current);
    }
}
