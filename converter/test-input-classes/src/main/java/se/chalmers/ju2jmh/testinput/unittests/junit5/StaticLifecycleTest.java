package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StaticLifecycleTest {

    @BeforeAll
    public static void globalSetup() {
        // become @Setup(Level.Trial)
    }

    @AfterAll
    public static void globalTeardown() {
        // become @TearDown(Level.Trial)
    }

    @Test
    public void benchmarkWorkload() {
        int a = 1 + 1;
    }
}