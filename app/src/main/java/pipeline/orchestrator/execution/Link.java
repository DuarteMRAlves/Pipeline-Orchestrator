package pipeline.orchestrator.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;

/*
 * Class to link to stages
 */
public class Link {

    private static final int MAX_QUEUE_SIZE = 1;
    private final List<LinkListener> listeners = new ArrayList<>();

    private final BlockingQueue<ComputationState> dataQueue
            = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);

    public void registerListener(LinkListener listener) {
        synchronized (dataQueue) {
            listeners.add(listener);
        }
    }

    public void put(ComputationState dynamicMessage)
            throws InterruptedException {
        synchronized (dataQueue) {
            if (dataQueue.size() == MAX_QUEUE_SIZE)
                dataQueue.take();
            dataQueue.put(dynamicMessage);
            listeners.forEach(l -> l.onNewObject(this));
        }
    }

    public ComputationState take() throws InterruptedException {
        return dataQueue.take();
    }

    /**
     * Takes computation states from the link until the predicate is satisfied
     * @param predicate predicate to satisfy
     * @return the first computation state that satisfies the predicate
     * @throws InterruptedException if the thread was interrupted while waiting
     */
    public ComputationState takeUntil(
            Predicate<ComputationState> predicate)
            throws InterruptedException {

        // Negation of the predicate so that
        // we can iterate while the predicate is not true
        Predicate<ComputationState> predicateNegation = predicate.negate();

        ComputationState computationState = dataQueue.take();
        while (predicateNegation.test(computationState)) {
            computationState = dataQueue.take();
        }
        return computationState;
    }
}
