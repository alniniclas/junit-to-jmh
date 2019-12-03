package se.chalmers.ju2jmh.experiments;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public interface Ju2JmhBenchmarkScaffold {
    default void before() {}

    default void after() {}

    default Statement applyRuleFields(Statement statement, Description description) {
        return statement;
    }

    default Statement applyRuleMethods(Statement statement, Description description) {
        return statement;
    }

    default void runBenchmark(BenchmarkMethod benchmark, Description description) throws Throwable {
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
        statement = applyRuleFields(statement, description);
        statement = applyRuleMethods(statement, description);
        statement.evaluate();
    }
}
