package com.example;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

import static org.openjdk.jmh.annotations.Level.Invocation;
import static org.openjdk.jmh.annotations.Level.Iteration;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class Annotations {
    public void notABenchmark() {
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmark() {
        setUp1();
        setUp2();
        setUp3();
        setUp4();
        try {
            notABenchmark();
        } finally {
            tearDown1();
            tearDown2();
            tearDown3();
            tearDown4();
        }
    }

    public void setUp1() {
    }

    public void setUp2() {
    }

    public void setUp3() {
    }

    public void setUp4() {
    }

    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Iteration)
    public void setUp5() {
    }

    @org.openjdk.jmh.annotations.Setup(Level.Iteration)
    public void setUp6() {
    }

    @org.openjdk.jmh.annotations.Setup(Iteration)
    public void setUp7() {
    }

    public void tearDown1() {
    }

    public void tearDown2() {
    }

    public void tearDown3() {
    }

    public void tearDown4() {
    }

    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Iteration)
    public void tearDown5() {
    }

    @org.openjdk.jmh.annotations.TearDown(Level.Iteration)
    public void tearDown6() {
    }

    @org.openjdk.jmh.annotations.TearDown(Iteration)
    public void tearDown7() {
    }
}
