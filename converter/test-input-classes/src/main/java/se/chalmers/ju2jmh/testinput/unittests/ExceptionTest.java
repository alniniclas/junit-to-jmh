package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class ExceptionTest {
    @Test(expected = Exception.class)
    public void testException() throws Exception {
        throw new Exception();
    }
}
