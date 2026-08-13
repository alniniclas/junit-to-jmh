package se.chalmers.ju2jmh.junit5;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StaticLifecycleTest {

    @BeforeAll
    public static void globalSetup() {
        //  @Setup(Level.Trial)
    }

    @AfterAll
    public static void globalTeardown() {
        //  @TearDown(Level.Trial)
    }

    @Test
    public void benchmarkWorkload() {
        int a = 1 + 1;
    }
}