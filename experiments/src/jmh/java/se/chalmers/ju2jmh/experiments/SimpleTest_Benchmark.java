package se.chalmers.ju2jmh.experiments;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(time = 100, timeUnit = TimeUnit.MILLISECONDS)
public class SimpleTest_Benchmark implements Ju2JmhBenchmarkScaffold {
    public static class Implementation {
        public void test() {
        }
    }

    @Benchmark
    public void test_invokeThroughStaticClass() throws Throwable {
        createImplementation();
        Description description = Description.createTestDescription(implementation().getClass(),
                "test");
        runBenchmark(implementation()::test, description);
    }

    @Benchmark
    public void invokeDirectly() {
        simpleTest.test();
    }

    @Benchmark
    public void test_invokeThroughReflection() throws Throwable {
        JUnit4TestInvoker.invoke(testClass, "test");
    }

    @Benchmark
    public Result test_invokeThroughJUnitRunner() {
        JUnitCore runner = new JUnitCore();
        return runner.run(Request.method(testClass, "test"));
    }

    private Class<SimpleTest> testClass = SimpleTest.class;
    private SimpleTest simpleTest = new SimpleTest();
    private Implementation implementation;

    public void createImplementation() {
        this.implementation = new Implementation();
    }

    public Implementation implementation() {
        return this.implementation;
    }
}
