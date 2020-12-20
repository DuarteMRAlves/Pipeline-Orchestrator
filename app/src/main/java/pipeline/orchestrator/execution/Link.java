package pipeline.orchestrator.execution;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;

/*
 * Class to link to stages
 */
public class Link {

    private static final int MAX_QUEUE_SIZE = 1;

    private final BlockingQueue<ComputationState> dataQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);

    public void put(ComputationState dynamicMessage) throws InterruptedException {
        synchronized (dataQueue) {
            if (dataQueue.size() == MAX_QUEUE_SIZE)
                dataQueue.take();
            dataQueue.put(dynamicMessage);
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
