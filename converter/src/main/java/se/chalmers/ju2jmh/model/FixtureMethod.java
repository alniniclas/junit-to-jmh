package se.chalmers.ju2jmh.model;

import java.util.Objects;

/**
 * Represents a fixture method of a unit test class.
 */
public class FixtureMethod {
    /**
     * Represents a fixture method kind. The four kinds are {@link org.junit.Before},
     * {@link org.junit.After}, {@link org.junit.BeforeClass}, and {@link org.junit.AfterClass}.
     */
    public enum Kind {
        BEFORE("@Before"), AFTER("@After"), BEFORE_CLASS("@BeforeClass"),
        AFTER_CLASS("@AfterClass");

        private final String annotation;

        Kind(String annotation) {
            this.annotation = annotation;
        }

        @Override
        public String toString() {
            return annotation;
        }
    }

    private final String name;
    private final Kind kind;

    private FixtureMethod(String name, Kind kind) {
        this.name = name;
        this.kind = kind;
    }

    /**
     * Returns the name of this fixture method.
     *
     * @return the fixture method name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the {@link Kind} of this fixture method.
     *
     * @return the fixture method kind
     */
    public Kind kind() {
        return kind;
    }

    /**
     * Returns whether this fixture method is static. {@link org.junit.BeforeClass} and
     * {@link org.junit.AfterClass} are static, {@link org.junit.Before} and {@link org.junit.After}
     * are not.
     *
     * @return true if this fixture method is static, false otherwise
     */
    public boolean isStatic() {
        switch (kind) {
            case BEFORE_CLASS:
            case AFTER_CLASS:
                return true;
            case BEFORE:
            case AFTER:
                return false;
            default:
                throw new AssertionError("Invalid fixture method kind.");
        }
    }

    /**
     * Creates a @Before fixture method with the given name.
     *
     * @param name the name of the fixture method
     * @return the created fixture method
     */
    public static FixtureMethod before(String name) {
        return new FixtureMethod(name, Kind.BEFORE);
    }


    /**
     * Creates an @After fixture method with the given name.
     *
     * @param name the name of the fixture method
     * @return the created fixture method
     */
    public static FixtureMethod after(String name) {
        return new FixtureMethod(name, Kind.AFTER);
    }

    /**
     * Creates a @BeforeClass fixture method with the given name.
     *
     * @param name the name of the fixture method
     * @return the created fixture method
     */
    public static FixtureMethod beforeClass(String name) {
        return new FixtureMethod(name, Kind.BEFORE_CLASS);
    }

    /**
     * Creates a @AfterClass fixture method with the given name.
     *
     * @param name the name of the fixture method
     * @return the created fixture method
     */
    public static FixtureMethod afterClass(String name) {
        return new FixtureMethod(name, Kind.AFTER_CLASS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FixtureMethod that = (FixtureMethod) o;
        return name.equals(that.name) && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind);
    }

    @Override
    public String toString() {
        return kind.toString() + " " + name + "();";
    }
}
