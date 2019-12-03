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
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(time = 100, timeUnit = TimeUnit.MILLISECONDS)
public class ComplexTestSubclass_Benchmark extends ComplexTestSuperclass_Benchmark
        implements Ju2JmhBenchmarkScaffold {
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
    public void subclassTest_invokeThroughStaticClass() throws Throwable {
        createImplementation();
        Description description = Description.createTestDescription(implementation().getClass(),
                "subclassTest");
        runBenchmark(implementation()::subclassTest, description);
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
    public void before() {
        super.before();
        implementation().superclassBefore();
    }

    @Override
    public void after() {
        super.after();
        implementation().superclassAfter();
    }

    @Override
    public Statement applyRuleFields(Statement statement, Description description) {
        statement = super.applyRuleFields(statement, description);
        statement = implementation().subclassRuleField.apply(statement, description);
        return statement;
    }

    @Override
    public Statement applyRuleMethods(Statement statement, Description description) {
        statement = super.applyRuleMethods(statement, description);
        statement = implementation().subclassRuleMethod().apply(statement, description);
        return statement;
    }
}
