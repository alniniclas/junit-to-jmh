package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class TwoTestCases_JU4Benchmark {
    private Class<?> testClass = TwoTestCases.class;
    private JUnitCore runner = new JUnitCore();

    private Result runBenchmark(String benchmark) {
        return this.runner.run(Request.method(this.testClass, benchmark));
    }

    @Benchmark
    public Result benchmark_test1() {
        return this.runBenchmark("test1");
    }

    @Benchmark
    public Result benchmark_test2() {
        return this.runBenchmark("test2");
    }
}
