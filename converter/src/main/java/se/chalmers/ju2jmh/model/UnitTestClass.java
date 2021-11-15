package se.chalmers.ju2jmh.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents a unit test class, containing tests, fixture methods, and test rules.
 */
public class UnitTestClass {
    private final String name;
    private final UnitTestClass superclass;
    private final List<UnitTest> tests;
    private final List<FixtureMethod> before;
    private final List<FixtureMethod> after;
    private final List<FixtureMethod> beforeClass;
    private final List<FixtureMethod> afterClass;
    private final List<TestRule> ruleFields;
    private final List<TestRule> ruleMethods;
    private final List<TestRule> classRuleFields;
    private final List<TestRule> classRuleMethods;

    private UnitTestClass(String name,
            UnitTestClass superclass,
            List<UnitTest> tests,
            List<FixtureMethod> before,
            List<FixtureMethod> after,
            List<FixtureMethod> beforeClass,
            List<FixtureMethod> afterClass,
            List<TestRule> ruleFields,
            List<TestRule> ruleMethods,
            List<TestRule> classRuleFields,
            List<TestRule> classRuleMethods) {
        this.name = name;
        this.superclass = superclass;
        this.tests = tests;
        this.before = before;
        this.after = after;
        this.beforeClass = beforeClass;
        this.afterClass = afterClass;
        this.ruleFields = ruleFields;
        this.ruleMethods = ruleMethods;
        this.classRuleFields = classRuleFields;
        this.classRuleMethods = classRuleMethods;
    }

    /**
     * Returns the name of this unit test class.
     *
     * @return the unit test class name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the superclass of this unit test class, if it has one.
     *
     * @return an {@link Optional} containing the unit test class superclass, if one exists, or an
     *     empty Optional otherwise
     */
    public Optional<UnitTestClass> superclass() {
        return Optional.ofNullable(superclass);
    }

    /**
     * Returns the list of unit tests declared by this unit test class.
     *
     * @return the list of declared unit tests
     */
    public List<UnitTest> tests() {
        return tests;
    }

    /**
     * Returns the list of {@link org.junit.Before} methods declared by this unit test class.
     *
     * @return the list of declared before methods
     */
    public List<FixtureMethod> before() {
        return before;
    }

    /**
     * Returns the list of {@link org.junit.After} methods declared by this unit test class.
     *
     * @return the list of declared after methods
     */
    public List<FixtureMethod> after() {
        return after;
    }

    /**
     * Returns the list of {@link org.junit.BeforeClass} methods declared by this unit test class.
     *
     * @return the list of declared before class methods
     */
    public List<FixtureMethod> beforeClass() {
        return beforeClass;
    }

    /**
     * Returns the list of {@link org.junit.AfterClass} methods declared by this unit test class.
     *
     * @return the list of declared after class methods
     */
    public List<FixtureMethod> afterClass() {
        return afterClass;
    }

    /**
     * Returns the list of {@link org.junit.Rule} fields declared by this unit test class.
     *
     * @return the list of declared rule fields
     */
    public List<TestRule> ruleFields() {
        return ruleFields;
    }

    /**
     * Returns the list of {@link org.junit.Rule} methods declared by this unit test class.
     *
     * @return the list of declared rule methods
     */
    public List<TestRule> ruleMethods() {
        return ruleMethods;
    }

    /**
     * Returns the list of {@link org.junit.ClassRule} fields declared by this unit test class.
     *
     * @return the list of declared rule fields
     */
    public List<TestRule> classRuleFields() {
        return classRuleFields;
    }

    /**
     * Returns the list of {@link org.junit.ClassRule} methods declared by this unit test class.
     *
     * @return the list of declared rule methods
     */
    public List<TestRule> classRuleMethods() {
        return classRuleMethods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnitTestClass testClass = (UnitTestClass) o;
        return name.equals(testClass.name) && Objects.equals(superclass, testClass.superclass)
                && tests.equals(testClass.tests) && before.equals(testClass.before)
                && after.equals(testClass.after) && beforeClass.equals(testClass.beforeClass)
                && afterClass.equals(testClass.afterClass)
                && ruleFields.equals(testClass.ruleFields)
                && ruleMethods.equals(testClass.ruleMethods)
                && classRuleFields.equals(testClass.classRuleFields)
                && classRuleMethods.equals(testClass.classRuleMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name, superclass, tests, before, after, beforeClass, afterClass, ruleFields,
                ruleMethods, classRuleFields, classRuleMethods);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.append(" {");
        if (superclass != null) {
            stringBuilder.append("\n")
                    .append("  extends ")
                    .append(superclass.toString().replace("\n", "\n  "));
        }
        Stream.of(beforeClass, afterClass, classRuleFields, classRuleMethods, before, after,
                        ruleFields, ruleMethods, tests)
                .map(Collection::stream)
                .reduce(Stream::concat)
                .orElse(Stream.empty())
                .map(Object::toString)
                .forEach(m -> stringBuilder.append("\n").append("  ").append(m));
        stringBuilder.append("\n").append("}");
        return stringBuilder.toString();
    }

    /**
     * Builder for creating {@link UnitTestClass}es.
     */
    public static class Builder {
        private final String name;
        private UnitTestClass superclass;
        private final List<UnitTest> tests = new ArrayList<>();
        private final List<FixtureMethod> before = new ArrayList<>();
        private final List<FixtureMethod> after = new ArrayList<>();
        private final List<FixtureMethod> beforeClass = new ArrayList<>();
        private final List<FixtureMethod> afterClass = new ArrayList<>();
        private final List<TestRule> ruleFields = new ArrayList<>();
        private final List<TestRule> ruleMethods = new ArrayList<>();
        private final List<TestRule> classRuleFields = new ArrayList<>();
        private final List<TestRule> classRuleMethods = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        /**
         * Adds the given superclass.
         *
         * @param superclass the superclass
         * @return a reference to this builder
         */
        public Builder withSuperclass(UnitTestClass superclass) {
            if (this.superclass != null) {
                throw new IllegalStateException("Superclass already set.");
            }
            this.superclass = superclass;
            return this;
        }

        /**
         * Adds a unit test with the given name.
         *
         * @param name the test name
         * @return a reference to this builder
         */
        public Builder withTest(String name) {
            tests.add(UnitTest.test(name));
            return this;
        }

        /**
         * Adds an exception unit test with the given name and exception type.
         *
         * @param name the test name
         * @param exception the fully qualified name of the expected exception
         * @return a reference to this builder
         */
        public Builder withExceptionTest(String name, String exception) {
            tests.add(UnitTest.exceptionTest(name, exception));
            return this;
        }

        /**
         * Adds a @Before method with the given name.
         *
         * @param name the before method name
         * @return a reference to this builder
         */
        public Builder withBefore(String name) {
            before.add(FixtureMethod.before(name));
            return this;
        }

        /**
         * Adds an @After method with the given name.
         *
         * @param name the after method name
         * @return a reference to this builder
         */
        public Builder withAfter(String name) {
            after.add(FixtureMethod.after(name));
            return this;
        }

        /**
         * Adds a @BeforeClass method with the given name.
         *
         * @param name the before class method name
         * @return a reference to this builder
         */
        public Builder withBeforeClass(String name) {
            beforeClass.add(FixtureMethod.beforeClass(name));
            return this;
        }

        /**
         * Adds a @AfterClass method with the given name.
         *
         * @param name the after class method name
         * @return a reference to this builder
         */
        public Builder withAfterClass(String name) {
            afterClass.add(FixtureMethod.afterClass(name));
            return this;
        }

        /**
         * Adds a @Rule field with the given name.
         *
         * @param name the rule field name
         * @return a reference to this builder
         */
        public Builder withInstanceRuleField(String name) {
            ruleFields.add(TestRule.fromField(name));
            return this;
        }

        /**
         * Adds a @Rule method with the given name.
         *
         * @param name the rule method name
         * @return a reference to this builder
         */
        public Builder withInstanceRuleMethod(String name) {
            ruleMethods.add(TestRule.fromMethod(name));
            return this;
        }

        /**
         * Adds a @ClassRule field with the given name.
         *
         * @param name the rule field name
         * @return a reference to this builder
         */
        public Builder withClassRuleField(String name) {
            classRuleFields.add(TestRule.fromStaticField(name));
            return this;
        }

        /**
         * Adds a @ClassRule method with the given name.
         *
         * @param name the rule method name
         * @return a reference to this builder
         */
        public Builder withClassRuleMethod(String name) {
            classRuleMethods.add(TestRule.fromStaticMethod(name));
            return this;
        }

        private static <T> void ensureUnique(
                String kind, Function<T, String> getName, List<T> list1, List<T> list2) {
            Set<String> names = new HashSet<>();
            for (T item : list1) {
                String name = getName.apply(item);
                if (names.contains(name)) {
                    throw new IllegalStateException("Duplicate " + kind + ": " + name);
                }
                names.add(name);
            }
            if (list2 != null) {
                for (T item : list2) {
                    String name = getName.apply(item);
                    if (names.contains(name)) {
                        throw new IllegalStateException("Duplicate " + kind + ": " + name);
                    }
                    names.add(name);
                }
            }
        }

        private static <T> void ensureUnique(
                String kind, Function<T, String> getName, List<T> list) {
            ensureUnique(kind, getName, list, null);
        }

        /**
         * Builds the unit test class represented by this builder.
         *
         * @return the built unit test class
         */
        public UnitTestClass build() {
            ensureUnique("test", UnitTest::name, tests);
            ensureUnique("before method", FixtureMethod::name, before, beforeClass);
            ensureUnique("after method", FixtureMethod::name, after, afterClass);
            ensureUnique("rule method", TestRule::name, ruleMethods, classRuleMethods);
            ensureUnique("rule field", TestRule::name, ruleFields, classRuleFields);
            return new UnitTestClass(
                    name, superclass, Collections.unmodifiableList(tests),
                    Collections.unmodifiableList(before), Collections.unmodifiableList(after),
                    Collections.unmodifiableList(beforeClass),
                    Collections.unmodifiableList(afterClass),
                    Collections.unmodifiableList(ruleFields),
                    Collections.unmodifiableList(ruleMethods),
                    Collections.unmodifiableList(classRuleFields),
                    Collections.unmodifiableList(classRuleMethods));
        }

        /**
         * Creates a new builder for the class with the given name.
         *
         * @param name the class name
         * @return the created builder
         */
        public static Builder forClass(String name) {
            return new Builder(name);
        }
    }
}
