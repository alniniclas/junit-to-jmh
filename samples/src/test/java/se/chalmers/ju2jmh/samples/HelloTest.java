package se.chalmers.ju2jmh.samples;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloTest {

    @Test
    public void testHello() {
        String expected = "Hello!";

        String output = Hello.hello();

        assertEquals(expected, output);
    }
}
