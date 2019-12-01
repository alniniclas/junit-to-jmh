package se.chalmers.ju2jmh.experiments;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class JUnitRunnerSimpleTestBenchmark {
    private Class<SimpleTest> testClass = SimpleTest.class;
    private SimpleTest simpleTest = new SimpleTest();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void invokeDirectly() {
        simpleTest.test();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void invokeThroughReflection() throws Throwable {
        JUnit4TestInvoker.invoke(testClass, "test");
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public Result invokeThroughJUnitRunner() {
        JUnitCore runner = new JUnitCore();
        return runner.run(Request.method(testClass, "test"));
    }
}
