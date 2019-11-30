package com.example;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class NoTearDown {
    public void notABenchmark() {
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmark1() {
        setUp1();
        setUp2();
        notABenchmark();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmark2() {
        setUp1();
        setUp2();
        notABenchmark();
        return 0;
    }

    public void setUp1() {
    }

    public void setUp2() {
    }
}
