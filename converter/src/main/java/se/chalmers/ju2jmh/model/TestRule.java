package se.chalmers.ju2jmh.model;

import java.util.Objects;

/**
 * Represents a test rule of a unit test class.
 */
public class TestRule {
    /**
     * Represents the source of a test rule. Can be either field or method.
     */
    public enum Source {
        FIELD, METHOD
    }

    private final String name;
    private final Source source;
    private final boolean isStatic;

    private TestRule(String name, Source source, boolean isStatic) {
        this.name = name;
        this.source = source;
        this.isStatic = isStatic;
    }

    /**
     * Returns the name of this test rule.
     *
     * @return the test rule name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the {@link Source} of this test rule.
     *
     * @return the test rule source
     */
    public Source source() {
        return source;
    }

    /**
     * Returns whether this test rule is static. {@link org.junit.ClassRule}s are static,
     * {@link org.junit.Rule}s are not.
     *
     * @return true if this test rule is static, false otherwise
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Creates a @Rule field with the given name.
     *
     * @param name the name of the test rule field
     * @return the created rule field
     */
    public static TestRule fromField(String name) {
        return new TestRule(name, Source.FIELD, false);
    }

    /**
     * Creates a @ClassRule field with the given name.
     *
     * @param name the name of the test rule field
     * @return the created rule field
     */
    public static TestRule fromStaticField(String name) {
        return new TestRule(name, Source.FIELD, true);
    }

    /**
     * Creates a @Rule method with the given name.
     *
     * @param name the name of the test rule method
     * @return the created rule method
     */
    public static TestRule fromMethod(String name) {
        return new TestRule(name, Source.METHOD, false);
    }


    /**
     * Creates a @ClassRule method with the given name.
     *
     * @param name the name of the test rule method
     * @return the created rule method
     */
    public static TestRule fromStaticMethod(String name) {
        return new TestRule(name, Source.METHOD, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestRule testRule = (TestRule) o;
        return isStatic == testRule.isStatic && name.equals(testRule.name)
                && source == testRule.source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source, isStatic);
    }

    @Override
    public String toString() {
        String annotation = isStatic ? "@ClassRule" : "@Rule";
        return annotation + " " + name + (source == Source.METHOD ? "()" : "") + ";";
    }
}
