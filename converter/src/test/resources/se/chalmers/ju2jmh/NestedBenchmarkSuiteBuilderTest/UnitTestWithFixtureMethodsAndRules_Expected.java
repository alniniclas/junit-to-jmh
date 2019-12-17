package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class UnitTestWithFixtureMethodsAndRules {
    @ClassRule
    public static final TestRule classRuleField1 = RuleChain.emptyRuleChain();

    @ClassRule
    public static final TestRule classRuleField2 = RuleChain.emptyRuleChain();

    @ClassRule
    public static TestRule classRuleMethod1() {
        return RuleChain.emptyRuleChain();
    }

    @ClassRule
    public static TestRule classRuleMethod2() {
        return RuleChain.emptyRuleChain();
    }

    @Rule
    public final TestRule ruleField1 = RuleChain.emptyRuleChain();

    @Rule
    public final TestRule ruleField2 = RuleChain.emptyRuleChain();

    @Rule
    public TestRule ruleMethod1() {
        return RuleChain.emptyRuleChain();
    }

    @Rule
    public TestRule ruleMethod2() {
        return RuleChain.emptyRuleChain();
    }

    @BeforeClass
    public static void beforeClass1() {}

    @BeforeClass
    public static void beforeClass2() {}

    @AfterClass
    public static void afterClass1() {}

    @AfterClass
    public static void afterClass2() {}

    @Before
    public void before1() {}

    @Before
    public void before2() {}

    @After
    public void after1() {}

    @After
    public void after2() {}

    @Test
    public void test() {}

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test, this.description("test"));
        }

        @java.lang.Override
        public void beforeClass() throws java.lang.Throwable {
            super.beforeClass();
            UnitTestWithFixtureMethodsAndRules.beforeClass1();
            UnitTestWithFixtureMethodsAndRules.beforeClass2();
        }

        @java.lang.Override
        public void afterClass() throws java.lang.Throwable {
            UnitTestWithFixtureMethodsAndRules.afterClass1();
            UnitTestWithFixtureMethodsAndRules.afterClass2();
            super.afterClass();
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before1();
            this.implementation().before2();
        }

        @java.lang.Override
        public void after() throws java.lang.Throwable {
            this.implementation().after1();
            this.implementation().after2();
            super.after();
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyClassRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(UnitTestWithFixtureMethodsAndRules.classRuleField1, statement, description);
            statement = this.applyRule(UnitTestWithFixtureMethodsAndRules.classRuleField2, statement, description);
            statement = super.applyClassRuleFields(statement, description);
            return statement;
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyClassRuleMethods(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(UnitTestWithFixtureMethodsAndRules.classRuleMethod1(), statement, description);
            statement = this.applyRule(UnitTestWithFixtureMethodsAndRules.classRuleMethod2(), statement, description);
            statement = super.applyClassRuleMethods(statement, description);
            return statement;
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().ruleField1, statement, description);
            statement = this.applyRule(this.implementation().ruleField2, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleMethods(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().ruleMethod1(), statement, description);
            statement = this.applyRule(this.implementation().ruleMethod2(), statement, description);
            statement = super.applyRuleMethods(statement, description);
            return statement;
        }

        private UnitTestWithFixtureMethodsAndRules implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new UnitTestWithFixtureMethodsAndRules();
        }

        @java.lang.Override
        public UnitTestWithFixtureMethodsAndRules implementation() {
            return this.implementation;
        }
    }
}
