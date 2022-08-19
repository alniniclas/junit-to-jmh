package se.chalmers.ju2jmh.api;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class JU2JmhBenchmarkTest {
    private static class LogRule implements TestRule {
        private final String name;
        private final List<String> eventLog;

        private LogRule(String name, List<String> eventLog) {
            this.name = name;
            this.eventLog = eventLog;
        }

        @Override
        public Statement apply(Statement statement, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    eventLog.add("start " + name);
                    try {
                        statement.evaluate();
                        eventLog.add("end " + name);
                    } finally {
                        eventLog.add("finally " + name);
                    }
                }
            };
        }
    }

    public static class LoggingUnitTest {
        protected static List<String> eventLog = new ArrayList<>();

        @org.junit.ClassRule
        public static LogRule classRuleField = new LogRule("classRuleField", eventLog);

        @org.junit.ClassRule
        public static LogRule classRuleMethod() {
            return new LogRule("classRuleMethod", eventLog);
        }

        @org.junit.Rule
        public LogRule ruleField = new LogRule("ruleField", eventLog);

        @org.junit.Rule
        public LogRule ruleMethod() {
            return new LogRule("ruleMethod", eventLog);
        }

        @org.junit.BeforeClass
        public static void beforeClassMethod() {
            eventLog.add("beforeClassMethod");
        }

        @org.junit.AfterClass
        public static void afterClassMethod() {
            eventLog.add("afterClassMethod");
        }

        @org.junit.Before
        public void beforeMethod() {
            eventLog.add("beforeMethod");
        }

        @org.junit.After
        public void afterMethod() {
            eventLog.add("afterMethod");
        }

        @org.junit.Test
        public void testMethod() {
            eventLog.add("testMethod");
        }

        public static void clearEventLog() {
            eventLog.clear();
        }

        public static List<String> getEventLog() {
            return new ArrayList<>(eventLog);
        }
    }

    @BeforeEach
    public void clearEventLog() {
        LoggingUnitTest.clearEventLog();
    }

    private static class BenchmarkImplementation extends JU2JmhBenchmark {
        private final LoggingUnitTest implementation;

        private BenchmarkImplementation(LoggingUnitTest implementation) {
            this.implementation = implementation;
        }

        @Override
        public Statement applyClassRuleFields(Statement statement, Description description) {
            return LoggingUnitTest.classRuleField.apply(statement, description);
        }

        @Override
        public Statement applyClassRuleMethods(Statement statement, Description description) {
            return LoggingUnitTest.classRuleMethod().apply(statement, description);
        }

        @Override
        public Statement applyRuleFields(Statement statement, Description description) {
            return implementation().ruleField.apply(statement, description);
        }

        @Override
        public Statement applyRuleMethods(Statement statement, Description description) {
            return implementation().ruleMethod().apply(statement, description);
        }

        @Override
        public void beforeClass() throws Throwable {
            LoggingUnitTest.beforeClassMethod();
        }

        @Override
        public void afterClass() throws Throwable {
            LoggingUnitTest.afterClassMethod();
        }

        @Override
        public void before() {
            implementation().beforeMethod();
        }

        @Override
        public void after() {
            implementation().afterMethod();
        }

        @Override
        public void createImplementation() {
        }

        @Override
        public LoggingUnitTest implementation() {
            return implementation;
        }
    }

    @Test
    public void executionOrderIsCorrect() throws Throwable {
        // Run with JUnit to get the correct evaluation order
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.run(Request.method(LoggingUnitTest.class, "testMethod"))
                .getFailures()
                .forEach(System.out::println);
        List<String> expected = LoggingUnitTest.getEventLog();
        LoggingUnitTest.clearEventLog();

        LoggingUnitTest test = new LoggingUnitTest();
        BenchmarkImplementation instance = new BenchmarkImplementation(test);
        instance.runBenchmark(test::testMethod, null);

        assertIterableEquals(expected, LoggingUnitTest.getEventLog());
    }

    public static class LoggingExceptionUnitTest extends LoggingUnitTest {
        @org.junit.Test(expected = Exception.class)
        public void exceptionTestMethod() throws Exception {
            eventLog.add("exceptionTestMethod");
            try {
                throw new Exception();
            } finally {
                eventLog.add("finally exceptionTestMethod");
            }
        }
    }

    private static class ExceptionBenchmarkImplementation extends BenchmarkImplementation {
        private final LoggingExceptionUnitTest implementation;

        private ExceptionBenchmarkImplementation(LoggingExceptionUnitTest implementation) {
            super(implementation);
            this.implementation = implementation;
        }

        @Override
        public LoggingExceptionUnitTest implementation() {
            return implementation;
        }
    }

    @Test
    public void exceptionExecutionOrderIsCorrect() throws Throwable {
        // Run with JUnit to get the correct evaluation order
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.run(Request.method(LoggingExceptionUnitTest.class, "exceptionTestMethod"))
                .getFailures()
                .forEach(e -> {
                    throw new AssertionError(e);
                });
        List<String> expected = LoggingUnitTest.getEventLog();
        LoggingUnitTest.clearEventLog();

        LoggingExceptionUnitTest test = new LoggingExceptionUnitTest();
        ExceptionBenchmarkImplementation instance = new ExceptionBenchmarkImplementation(test);
        instance.runExceptionBenchmark(test::exceptionTestMethod, null, Exception.class);

        assertIterableEquals(expected, LoggingUnitTest.getEventLog());
    }

    public static class LoggingExpectedExceptionRuleUnitTest extends LoggingUnitTest {
        @SuppressWarnings("deprecation")
        public ExpectedException expectedException = ExpectedException.none();

        @Rule
        public TestRule expectedExceptionRule =
                RuleChain.outerRule(new LogRule("expectedExceptionRule", eventLog))
                        .around(expectedException);

        @org.junit.Test
        public void exceptionTestMethod() throws Exception {
            eventLog.add("exceptionTestMethod");
            expectedException.expect(Exception.class);
            try {
                throw new Exception();
            } finally {
                eventLog.add("finally exceptionTestMethod");
            }
        }
    }

    private static class ExpectedExceptionRuleBenchmarkImplementation
            extends BenchmarkImplementation {
        private final LoggingExpectedExceptionRuleUnitTest implementation;

        private ExpectedExceptionRuleBenchmarkImplementation(
                LoggingExpectedExceptionRuleUnitTest implementation) {
            super(implementation);
            this.implementation = implementation;
        }

        @Override
        public LoggingExpectedExceptionRuleUnitTest implementation() {
            return implementation;
        }


        @Override
        public Statement applyRuleFields(Statement statement, Description description) {
            statement = implementation().expectedExceptionRule.apply(statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }
    }

    @Test
    public void expectedExceptionRuleExecutionOrderIsCorrect() throws Throwable {
        // Run with JUnit to get the correct evaluation order
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.run(Request.method(
                LoggingExpectedExceptionRuleUnitTest.class, "exceptionTestMethod"))
                .getFailures()
                .forEach(e -> {
                    throw new AssertionError(e);
                });
        List<String> expected = LoggingUnitTest.getEventLog();
        LoggingUnitTest.clearEventLog();

        LoggingExpectedExceptionRuleUnitTest test = new LoggingExpectedExceptionRuleUnitTest();
        ExpectedExceptionRuleBenchmarkImplementation instance =
                new ExpectedExceptionRuleBenchmarkImplementation(test);
        instance.runBenchmark(test::exceptionTestMethod, null);

        assertIterableEquals(expected, LoggingUnitTest.getEventLog());
    }

    private static class EmptyJU2JmhBenchmark extends JU2JmhBenchmark {
        private Object implementation;

        @Override
        public void createImplementation() {
            this.implementation = new Object();
        }

        @Override
        public Object implementation() {
            return this.implementation;
        }
    }

    private static class EmptyStatement extends Statement {
        @Override
        public void evaluate() {}
    }

    @Test
    public void timeoutRulesAreIgnored() throws Throwable {
        JU2JmhBenchmark benchmark = new EmptyJU2JmhBenchmark();
        Statement statement = new EmptyStatement();
        benchmark.createImplementation();
        Timeout timeoutRule = Timeout.seconds(1);

        Statement result = benchmark.applyRule(timeoutRule, statement, Description.EMPTY);

        assertEquals(statement, result);
    }

    @Test
    public void nonTimeoutRulesAreApplied() throws Throwable {
        JU2JmhBenchmark benchmark = new EmptyJU2JmhBenchmark();
        Statement statement = new EmptyStatement();
        Statement newStatement = new EmptyStatement();
        benchmark.createImplementation();
        TestRule rule = (base, description) -> newStatement;

        Statement result = benchmark.applyRule(rule, statement, Description.EMPTY);

        assertEquals(newStatement, result);
    }
}
