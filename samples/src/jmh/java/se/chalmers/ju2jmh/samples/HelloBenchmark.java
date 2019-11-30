package se.chalmers.ju2jmh.samples;

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
