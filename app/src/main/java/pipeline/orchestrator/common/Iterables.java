package pipeline.orchestrator.common;

import java.util.Optional;
import java.util.function.Function;

/**
 * Helper static class for operations on iterables
 */
public class Iterables {

    private Iterables() {}

    /**
     * Returns the first non null value generated by applying the callback function
     * over each element of the iterable. This method might not pass through every
     * element as it stops on the first found element
     * @param elements elements to iterate over
     * @param callback function to call on each element
     * @param <T> type of the element iterables
     * @param <R> return type for the callback function
     * @return optional with the first non null value if it exists or empty otherwise
     */
    public static <T, R> Optional<R> findFirstNonNull(
            Iterable<T> elements,
            Function<T, R> callback) {

        for (T el : elements) {
            R result = callback.apply(el);
            if (result != null) return Optional.of(result);
        }
        return Optional.empty();
    }
}
