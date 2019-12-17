package se.chalmers.ju2jmh;

import java.util.Optional;

/**
 * Utility class for manipulating class names.
 */
public class ClassNames {
    private ClassNames() {
        throw new AssertionError("Should not be instantiated.");
    }

    /**
     * Returns the name of the enclosing class if the given class name refers to a nested class, or
     * an empty {@link Optional} otherwise.
     */
    public static Optional<String> enclosingClassName(String className) {
        int lastNestedClassSeparatorIndex = className.lastIndexOf('$');
        if (lastNestedClassSeparatorIndex < 0) {
            return Optional.empty();
        } else {
            return Optional.of(className.substring(0, lastNestedClassSeparatorIndex));
        }
    }

    /**
     * Returns the name of the enclosing class if the given class is a nested class, or an empty
     * {@link Optional} otherwise.
     */
    public static Optional<String> enclosingClassName(Class<?> clazz) {
        return enclosingClassName(clazz.getName());
    }

    /**
     * Returns the name of the outermost enclosing class if the given class name refers to a nested
     * class, or the class name itself if the class it refers to is not nested.
     */
    public static String outermostClassName(String className) {
        int nestedClassSeparatorIndex = className.indexOf('$');
        if (nestedClassSeparatorIndex < 0) {
            return className;
        }
        return className.substring(0, nestedClassSeparatorIndex);
    }

    /**
     * Returns the name of the outermost enclosing class if the given class is a nested class, or
     * the name of the class itself if it is not.
     */
    public static String outermostClassName(Class<?> clazz) {
        return outermostClassName(clazz.getName());
    }

    /**
     * Returns the given class name with the package removed.
     */
    public static String shortClassName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * Returns the name of the given class with the package removed.
     */
    public static String shortClassName(Class<?> clazz) {
        return shortClassName(clazz.getName());
    }

    /**
     * Returns the given class name with the package and any enclosing classes removed.
     */
    public static String simpleClassName(String className) {
        return className.substring(
                Math.max(className.lastIndexOf('.'), className.lastIndexOf('$')) + 1);
    }

    /**
     * Returns the name of the given class with the package and any enclosing classes removed.
     */
    public static String simpleClassName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

    /**
     * Returns the canonical name of the class with the given name.
     */
    public static String canonicalClassName(String className) {
        return className.replace('$', '.');
    }

    /**
     * Returns the canonical name of the given class.
     */
    public static String canonicalClassName(Class<?> clazz) {
        return clazz.getCanonicalName();
    }

    /**
     * Returns the <i>binary name</i> (JVMS 4.2.1) of the given class name.
     */
    public static String binaryClassName(String className) {
        return className.replace('.', '/');
    }

    /**
     * Returns the <i>binary name</i> (JVMS 4.2.1) of the given class.
     */
    public static String binaryClassName(Class<?> clazz) {
        return binaryClassName(clazz.getName());
    }
}
