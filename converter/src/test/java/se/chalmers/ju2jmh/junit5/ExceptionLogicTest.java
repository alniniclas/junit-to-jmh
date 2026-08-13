package se.chalmers.ju2jmh.junit5;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExceptionLogicTest {

    @Test
    public void testExpectedException() {
        Assertions.assertThrows(NumberFormatException.class, () -> {
            Integer.parseInt("is not a number");
        });
    }

    @Test
    public void testNoException() {
        Integer.parseInt("12345");
    }
}