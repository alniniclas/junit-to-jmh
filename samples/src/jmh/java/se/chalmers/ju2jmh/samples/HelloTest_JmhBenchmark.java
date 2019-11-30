package se.chalmers.ju2jmh.samples;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Benchmark generated from HelloTest.
 */
@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class HelloTest_JmhBenchmark {

    @Test
    @org.openjdk.jmh.annotations.Benchmark
    public void testHello() {
        String expected = "Hello!";
        String output = Hello.hello();
        assertEquals(expected, output);
    }
}