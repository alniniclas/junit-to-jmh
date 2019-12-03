package se.chalmers.ju2jmh.experiments;

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
public class ComplexTestSuperclass_Benchmark implements Ju2JmhBenchmarkScaffold {
    public static class Implementation {
        public TestRule superclassRuleField = RuleChain.emptyRuleChain();

        public TestRule superclassRuleMethod() {
            return RuleChain.emptyRuleChain();
        }

        public void superclassBefore() {}

        public void superclassAfter() {}

        public void superclassTest() {}
    }

    @Benchmark
    public void superclassTest_invokeThroughStaticClass() throws Throwable {
        createImplementation();
        Description description = Description.createTestDescription(implementation().getClass(),
                "superclassTest");
        runBenchmark(implementation()::superclassTest, description);
    }

    @Benchmark
    public void superclassTest_invokeThroughReflection() throws Throwable {
        JUnit4TestInvoker.invoke(testClass(), "superclassTest");
    }

    @Benchmark
    public Result superclassTest_invokeThroughJUnitRunner() {
        JUnitCore runner = new JUnitCore();
        return runner.run(Request.method(testClass(), "superclassTest"));
    }

    protected Class<? extends ComplexTestSuperclass> testClass() {
        return ComplexTestSuperclass.class;
    }

    private Implementation implementation;

    public void createImplementation() {
        this.implementation = new Implementation();
    }

    public Implementation implementation() {
        return this.implementation;
    }

    @Override
    public void before() {
        implementation().superclassBefore();
    }

    @Override
    public void after() {
        implementation().superclassAfter();
    }

    @Override
    public Statement applyRuleFields(Statement statement, Description description) {
        statement = implementation().superclassRuleField.apply(statement, description);
        return statement;
    }

    @Override
    public Statement applyRuleMethods(Statement statement, Description description) {
        statement = implementation().superclassRuleMethod().apply(statement, description);
        return statement;
    }
}
