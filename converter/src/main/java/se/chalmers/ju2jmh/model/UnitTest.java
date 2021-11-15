package se.chalmers.ju2jmh.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a unit test.
 */
public class UnitTest {
    private final String name;
    private final String expectedException;

    private UnitTest(String name, String expectedException) {
        this.name = name;
        this.expectedException = expectedException;
    }

    /**
     * Returns the name of this unit test.
     *
     * @return the unit test name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the name of the expected exception type of this unit test, if one exists.
     *
     * @return an {@link Optional} containing the name of the expected exception type if there is
     *     one, or an empty Optional otherwise
     */
    public Optional<String> expectedException() {
        return Optional.ofNullable(expectedException);
    }

    /**
     * Creates a regular unit test with the given name, and no expected exception.
     *
     * @param name the unit test name
     * @return the created unit test
     */
    public static UnitTest test(String name) {
        return new UnitTest(name, null);
    }


    /**
     * Creates an exception unit test with the given name and expected exception.
     *
     * @param name the unit test name
     * @param expectedException the fully qualified type name of the expected exception
     * @return the created unit test
     */
    public static UnitTest exceptionTest(String name, String expectedException) {
        return new UnitTest(name, expectedException);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnitTest unitTest = (UnitTest) o;
        return name.equals(unitTest.name) && Objects.equals(expectedException,
                unitTest.expectedException);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, expectedException);
    }

    @Override
    public String toString() {
        String annotation = "@Test";
        if (expectedException != null) {
            annotation += "(expected=" + expectedException + ")";
        }
        return annotation + " " + name + "();";
    }
}
