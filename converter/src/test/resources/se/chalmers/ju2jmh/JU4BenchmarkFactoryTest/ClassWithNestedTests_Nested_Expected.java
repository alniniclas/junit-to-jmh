package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class ClassWithNestedTests_Nested_JU4Benchmark {
    private Class<?> testClass = ClassWithNestedTests.Nested.class;
    private JUnitCore runner = new JUnitCore();

    private Result runBenchmark(String benchmark) {
        return this.runner.run(Request.method(this.testClass, benchmark));
    }

    @Benchmark
    public Result benchmark_nestedTest() {
        return this.runBenchmark("nestedTest");
    }
}
