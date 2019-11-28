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
        notABenchmark();
    }

    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp1() {
    }

    @org.openjdk.jmh.annotations.Setup(Level.Invocation)
    public void setUp2() {
    }

    @org.openjdk.jmh.annotations.Setup(Invocation)
    public void setUp3() {
    }

    @Setup(org.openjdk.jmh.annotations.Level.Invocation)
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

    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Invocation)
    public void tearDown1() {
    }

    @org.openjdk.jmh.annotations.TearDown(Level.Invocation)
    public void tearDown2() {
    }

    @org.openjdk.jmh.annotations.TearDown(Invocation)
    public void tearDown3() {
    }

    @TearDown(org.openjdk.jmh.annotations.Level.Invocation)
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
