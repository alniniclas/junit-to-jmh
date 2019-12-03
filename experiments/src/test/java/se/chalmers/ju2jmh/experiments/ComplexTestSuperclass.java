package se.chalmers.ju2jmh.experiments;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class ComplexTestSuperclass {
    @Rule
    public TestRule superclassRuleField = RuleChain.emptyRuleChain();

    @Rule
    public TestRule superclassRuleMethod() {
        return RuleChain.emptyRuleChain();
    }

    @Before
    public void superclassBefore() {}

    @After
    public void superclassAfter() {}

    @Test
    public void superclassTest() {}
}
