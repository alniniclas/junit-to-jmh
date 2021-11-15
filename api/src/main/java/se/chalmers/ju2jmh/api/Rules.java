package se.chalmers.ju2jmh.api;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * Utility class containing methods for dealing with JUnit test rules.
 */
public class Rules {
    private Rules() {
        throw new AssertionError("Should not be instantiated.");
    }

    /**
     * Applies the given {@link TestRule} to the given {@link Statement}.
     *
     * {@link Timeout} rules are not applied.
     *
     * @param rule the rule to apply
     * @param statement the statement to apply the rule to
     * @param description a {@link Description} of the relevant test
     * @return a new statement with the rule applied
     */
    public static Statement apply(TestRule rule, Statement statement,  Description description) {
        if (rule.getClass() == Timeout.class) {
            return statement;
        }
        return rule.apply(statement, description);
    }

    /**
     * Applies the given {@link MethodRule} to the given {@link Statement}.
     *
     * @param rule the rule to apply
     * @param statement the statement to apply the rule to
     * @param method {@link FrameworkMethod} describing the relevant test
     * @param target the test class instance that will be used to run the test
     * @param <T> the test class type
     * @return a new statement with the rule applied
     */
    public static <T> Statement apply(
            MethodRule rule, Statement statement, FrameworkMethod method, T target) {
        return rule.apply(statement, method, target);
    }

    /**
     * Creates a {@link Description} for the test with the given name of the given class.
     *
     * @param clazz the test class
     * @param name the test name
     * @return the created description
     */
    public static Description description(Class<?> clazz, String name) {
        return Description.createTestDescription(clazz, name);
    }

    /**
     * Creates a {@link FrameworkMethod} for the test with the given name of the given class.
     *
     * @param clazz the test class
     * @param name the test name
     * @return the created framework method
     */
    public static FrameworkMethod frameworkMethod(Class<?> clazz, String name) {
        try {
            return new FrameworkMethod(clazz.getMethod(name));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
