package pipeline.orchestrator.common;

import java.util.function.Supplier;

/**
 * Helper static class for assuring the verification
 * of conditions throughout the code
 */
public class Conditions {

    private Conditions() {}

    /**
     * Verifies if the condition is true and throws an
     * exception if not
     * @param condition condition to verify
     * @param exceptionSupplier supplier to build the exception
     *                          to throw when the condition is false
     * @param <T> class for the exception
     * @throws T if the condition does not verify
     */
    public static <T extends Throwable> void checkState(
            boolean condition,
            Supplier<T> exceptionSupplier)
            throws T {

        if (!condition) {
            throw exceptionSupplier.get();
        }
    }
}
