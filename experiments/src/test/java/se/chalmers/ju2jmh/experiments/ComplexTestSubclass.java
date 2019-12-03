package se.chalmers.ju2jmh.experiments;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class ComplexTestSubclass extends ComplexTestSuperclass {
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
