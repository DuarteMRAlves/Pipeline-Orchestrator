package pipeline.orchestrator.common;

import com.google.common.base.Preconditions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Helper static class for managing future computations
 */
public class Futures {

    private Futures() {}

    /**
     * Waits the computation of a future object
     * @param future future object to execute
     * @param exceptionSupplier supplier for exceptions to throw if the computation fails with an execution error
     * @param <T> return type for the future object
     * @param <E> type for the thrown exception
     * @return the object returned by the future computation
     * @throws E the provided exception with the cause initialized to the execution error cause
     * @throws InterruptedException interrupted exception if interrupted while waiting for the computation
     */
    public static <T, E extends Throwable> T waitComputation(
            Future<T> future,
            Supplier<E> exceptionSupplier)
            throws E, InterruptedException {

        Preconditions.checkNotNull(future);
        Preconditions.checkNotNull(exceptionSupplier);

        try {
            return future.get();
        } catch (ExecutionException cause) {
            E newException = exceptionSupplier.get();
            newException.initCause(cause.getCause());
            throw newException;
        }
    }
}
