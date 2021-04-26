package pipeline.orchestrator.execution.inputs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;
import pipeline.orchestrator.execution.LinkListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Input Stream that receives entire messages from multiple sources
 * The messages are all collected and sent to the next stage
 * with no specific order between them
 */
public class CollectorInputStream
        implements StageInputStream, LinkListener {

    private final BlockingQueue<ComputationState> collectingQueue =
            new LinkedBlockingQueue<>();

    public CollectorInputStream(
            ImmutableSetMultimap<String, Link> inputs) {

        Preconditions.checkArgument(canBuildFrom(inputs));
        inputs.get("").forEach(link -> link.registerListener(this));
    }

    @Override
    public boolean isSource() {
        return false;
    }

    static boolean canBuildFrom(
            ImmutableSetMultimap<String, Link> outputs) {

        // Only contains empty key
        ImmutableSet<String> keySet = outputs.keySet();
        return keySet.size() == 1 && keySet.contains("");
    }

    @Override
    public ComputationState get() {
        try {
            return collectingQueue.take();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void onNewObject(Link link) {
        try {
            collectingQueue.add(link.take());
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
