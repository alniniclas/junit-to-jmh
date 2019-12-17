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
}
