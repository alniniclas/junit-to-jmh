package se.chalmers.ju2jmh.junit5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BasicLifecycleTest {

    @BeforeEach
    public void init() {
        //  @Setup(Level.Iteration)
    }

    @AfterEach
    public void tearDown() {
        //  @TearDown(Level.Iteration)
    }

    @Test
    public void testMethodOne() {
        // Benchmark 1
        Math.log(Math.random());
    }

    @Test
    public void testMethodTwo() {
        // Benchmark 2
        Math.log(Math.random());
    }
}