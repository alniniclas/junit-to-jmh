package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class TestImplementationSubclass_JU4Benchmark {
    private Class<?> testClass = TestImplementationSubclass.class;
    private JUnitCore runner = new JUnitCore();

    private Result runBenchmark(String benchmark) {
        return this.runner.run(Request.method(this.testClass, benchmark));
    }

    @Benchmark
    public Result benchmark_abstractClassTest() {
        return this.runBenchmark("abstractClassTest");
    }

    @Benchmark
    public Result benchmark_implementationTest() {
        return this.runBenchmark("implementationTest");
    }

    @Benchmark
    public Result benchmark_subclassTest() {
        return this.runBenchmark("subclassTest");
    }
}
