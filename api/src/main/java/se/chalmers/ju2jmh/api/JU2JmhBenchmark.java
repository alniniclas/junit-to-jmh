package se.chalmers.ju2jmh.api;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public abstract class JU2JmhBenchmark {
    public interface BenchmarkMethod {
        void run() throws Throwable;
    }

    public abstract void createImplementation() throws Throwable;

    public abstract Object implementation();

    public void beforeClass() throws Throwable {}

    public void afterClass() throws Throwable {}

    public void before() throws Throwable {}

    public void after() throws Throwable {}

    public Statement applyClassRuleFields(Statement statement, Description description) {
        return statement;
    }

    public Statement applyClassRuleMethods(Statement statement, Description description) {
        return statement;
    }

    public Statement applyRuleFields(Statement statement, Description description) {
        return statement;
    }

    public Statement applyRuleMethods(Statement statement, Description description) {
        return statement;
    }

    public final Description description(String methodName) {
        return Description.createTestDescription(implementation().getClass(), methodName);
    }

    private FrameworkMethod frameworkMethodFromDescription(Description description) {
        FrameworkMethod frameworkMethod;
        try {
            frameworkMethod = new FrameworkMethod(
                    implementation().getClass().getMethod(description.getMethodName()));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return frameworkMethod;
    }

    public final Statement applyRule(TestRule rule, Statement statement, Description description) {
        return rule.apply(statement, description);
    }

    public final Statement applyRule(MethodRule rule, Statement statement,
            Description description) {
        return rule.apply(statement, frameworkMethodFromDescription(description), implementation());
    }

    public final void runBenchmark(BenchmarkMethod benchmark, Description description)
            throws Throwable {
        Statement statement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                try {
                    benchmark.run();
                } finally {
                    after();
                }
            }
        };
        statement = applyClassRuleFields(statement, description);
        statement = applyClassRuleMethods(statement, description);
        statement = applyRuleFields(statement, description);
        statement = applyRuleMethods(statement, description);
        statement.evaluate();
    }

    public final void runExceptionBenchmark(BenchmarkMethod benchmark, Description description,
                                            Class<? extends Throwable> expected) throws Throwable {
        try {
            runBenchmark(benchmark, description);
        } catch (Throwable e) {
            if (expected.isInstance(e)) {
                return;
            }
            throw e;
        }
        throw new AssertionError(
                "Expected " + expected.getCanonicalName() + " but none was thrown");
    }
}
