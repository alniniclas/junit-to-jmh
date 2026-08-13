package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionLogicTest {

    //exception logic test, should be converted to a benchmark that expects an exception to be thrown
    @Test
    public void testExpectedException() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
            Integer.parseInt("Non è un numero");
        });
    }

    @Test
    public void testNoException() {
        Integer.parseInt("12345");
    }
}