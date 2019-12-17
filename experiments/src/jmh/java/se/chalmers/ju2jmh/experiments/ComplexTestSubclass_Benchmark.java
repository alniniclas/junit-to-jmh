package se.chalmers.ju2jmh.experiments;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runners.model.Statement;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(time = 100, timeUnit = TimeUnit.MILLISECONDS)
public class ComplexTestSubclass_Benchmark extends ComplexTestSuperclass_Benchmark {
    public static class Implementation extends ComplexTestSuperclass_Benchmark.Implementation {
        @Rule
        public TestRule subclassRuleField = RuleChain.emptyRuleChain();

        @Rule
        public TestRule subclassRuleMethod() {
            return RuleChain.emptyRuleChain();
        }

        @Before
        public void subclassBefore() {}

        @After
        public void subclassAfter() {}

        @Test
        public void subclassTest() {}
    }

    @Benchmark
    public void subclassTest_invokeThroughMethodReference() throws Throwable {
        this.createImplementation();
        this.runBenchmark(this.implementation()::subclassTest, this.description("subclassTest"));
    }

    @Benchmark
    public void subclassTest_invokeThroughReflection() throws Throwable {
        JUnit4TestInvoker.invoke(testClass(), "subclassTest");
    }

    @Benchmark
    public Result subclassTest_invokeThroughJUnitRunner() {
        JUnitCore runner = new JUnitCore();
        return runner.run(Request.method(testClass(), "subclassTest"));
    }

    @Override
    protected Class<? extends ComplexTestSubclass> testClass() {
        return ComplexTestSubclass.class;
    }

    private Implementation implementation;

    @Override
    public void createImplementation() {
        this.implementation = new Implementation();
    }

    @Override
    public Implementation implementation() {
        return this.implementation;
    }

    @Override
    public void before() throws Throwable {
        super.before();
        implementation().superclassBefore();
    }

    @Override
    public void after() throws Throwable {
        implementation().superclassAfter();
        super.after();
    }

    @Override
    public Statement applyRuleFields(Statement statement, Description description) {
        statement = implementation().subclassRuleField.apply(statement, description);
        statement = super.applyRuleFields(statement, description);
        return statement;
    }

    @Override
    public Statement applyRuleMethods(Statement statement, Description description) {
        statement = implementation().subclassRuleMethod().apply(statement, description);
        statement = super.applyRuleMethods(statement, description);
        return statement;
    }
}
