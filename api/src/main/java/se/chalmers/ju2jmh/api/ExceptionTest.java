package se.chalmers.ju2jmh.api;

/**
 * Wrapper object for unit tests expected to throw exceptions.
 *
 * @param <T> the class that the unit test is declared in
 */
public class ExceptionTest<T> implements ThrowingConsumer<T> {
    private final ThrowingConsumer<T> test;
    private final Class<? extends Throwable> expected;

    /**
     * Constructs a new exception test for the given unit test, expecting the given exception.
     *
     * @param test the unit test
     * @param expected the class of the expected exception
     */
    public ExceptionTest(ThrowingConsumer<T> test, Class<? extends Throwable> expected) {
        this.test = test;
        this.expected = expected;
    }

    /**
     * Executes this exception test using the given test class instance.
     *
     * @param t an instance of the unit test class that this test is declared in
     * @throws AssertionError if the test completed without throwing an exception
     * @throws Throwable if an exception of a different type than the expected one was thrown
     */
    @Override
    public void accept(T t) throws Throwable {
        try {
            test.accept(t);
        } catch (Throwable e) {
            if (expected.isInstance(e)) {
                return;
            }
            throw e;
        }
        throw new AssertionError(
                "Expected " + expected.getCanonicalName() + " but none was thrown");
    }
}
