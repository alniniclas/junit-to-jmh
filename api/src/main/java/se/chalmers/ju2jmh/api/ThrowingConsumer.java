package se.chalmers.ju2jmh.api;

/**
 * A consumer capable of throwing exceptions, errors, and other throwables.
 *
 * @param <T> the type of input accepted by this consumer
 */
@FunctionalInterface
public interface ThrowingConsumer<T> {

    /**
     * Consume the given input.
     *
     * @param t the input to consume
     * @throws Throwable if an error occurs
     */
    void accept(T t) throws Throwable;
}