package se.chalmers.ju2jmh.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

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
            return implementation.ruleField.apply(statement, description);
        }

        @Override
        public Statement applyRuleMethods(Statement statement, Description description) {
            return implementation.ruleMethod().apply(statement, description);
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
            implementation.beforeMethod();
        }

        @Override
        public void after() {
            implementation.afterMethod();
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
                .forEach(System.out::println);
        List<String> expected = LoggingUnitTest.getEventLog();
        LoggingUnitTest.clearEventLog();

        LoggingExceptionUnitTest test = new LoggingExceptionUnitTest();
        ExceptionBenchmarkImplementation instance = new ExceptionBenchmarkImplementation(test);
        instance.runExceptionBenchmark(test::exceptionTestMethod, null, Exception.class);

        assertIterableEquals(expected, LoggingUnitTest.getEventLog());
    }
}
