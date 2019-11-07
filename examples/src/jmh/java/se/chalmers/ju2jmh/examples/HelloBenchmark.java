package se.chalmers.ju2jmh.examples;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * Manually written benchmark.
 */
public class HelloBenchmark {

    @Benchmark
    public String benchmarkHello() {
        return Hello.hello();
    }
}
