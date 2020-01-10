package se.chalmers.ju2jmh.experiments.basebenchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class ConstantFolding {
    private final double finalInput = 25.0;
    private double input = 25.0;

    @Benchmark
    public void baseline() {}

    @Benchmark
    public String inline() {
        return Double.toHexString(25.0);
    }

    @Benchmark
    public String finalField() {
        return Double.toHexString(finalInput);
    }

    @Benchmark
    public String nonFinalField() {
        return Double.toHexString(input);
    }
}
