package com.example;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class ReturnValueNoTearDown {
    public void notABenchmark() {
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmark1() {
        notABenchmark();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmark2() {
        notABenchmark();
        return 0;
    }

    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp1() {
    }

    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp2() {
    }
}
