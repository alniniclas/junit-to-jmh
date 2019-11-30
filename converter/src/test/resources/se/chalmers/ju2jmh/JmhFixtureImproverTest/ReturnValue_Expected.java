package com.example;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class ReturnValue {
    public void notABenchmark() {
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void benchmark1() {
        setUp1();
        setUp2();
        try {
            notABenchmark();
        } finally {
            tearDown1();
            tearDown2();
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmark2() {
        setUp1();
        setUp2();
        try {
            notABenchmark();
            return 0;
        } finally {
            tearDown1();
            tearDown2();
        }
    }

    public void setUp1() {
    }

    public void setUp2() {
    }

    public void tearDown1() {
    }

    public void tearDown2() {
    }
}
