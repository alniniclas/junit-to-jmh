package se.chalmers.ju2jmh.experiments.basebenchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public class DeadCodeElimination {
    private double input = Math.PI;

    @Benchmark
    public void baseline() {}

    @Benchmark
    public void notConsumed() {
        Double.toHexString(input);
    }

    @Benchmark
    public String returned() {
        return Double.toHexString(input);
    }

    @Benchmark
    public void consumedByBlackhole(Blackhole blackhole) {
        blackhole.consume(Double.toHexString(input));
    }
}
