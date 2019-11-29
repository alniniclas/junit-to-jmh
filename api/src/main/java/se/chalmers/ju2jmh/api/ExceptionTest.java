package se.chalmers.ju2jmh.api;

public interface ExceptionTest {
    static void assertThrows(Class<? extends Throwable> expected, ExceptionTest test) {
        try {
            test.run();
        } catch (Throwable e) {
            if (expected.isInstance(e)) {
                return;
            }
            throw new AssertionError(
                    "Expected " + expected.getCanonicalName()
                            + " but got " + e.getClass().getCanonicalName(), e);
        }
        throw new AssertionError(
                "Expected " + expected.getCanonicalName() + " but got none");
    }

    void run() throws Throwable;
}
