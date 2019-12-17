package se.chalmers.ju2jmh;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.Method;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public class Bytecode {
    private Bytecode() {
        throw new AssertionError("Should not be instantiated.");
    }

    public static class Predicates {
        private Predicates() {
            throw new AssertionError("Should not be instantiated.");
        }

        private static <T extends FieldOrMethod> Predicate<T> isFieldOrMethodAnnotated(
                Class<? extends Annotation> annotationType) {
            return fom -> Arrays.stream(fom.getAnnotationEntries())
                    .map(AnnotationEntry::getAnnotationType)
                    .anyMatch(Predicate.isEqual(annotationTypeName(annotationType)));
        }

        public static Predicate<Method> isMethodAnnotated(Class<? extends Annotation> annotationType) {
            return isFieldOrMethodAnnotated(annotationType);
        }

        public static Predicate<Field> isFieldAnnotated(Class<? extends Annotation> annotationType) {
            return isFieldOrMethodAnnotated(annotationType);
        }

        public static Predicate<Method> hasArgCount(int n) {
            return m -> m.getArgumentTypes().length == n;
        }
    }

    public static String referenceFieldTypeDescriptor(Class<?> type) {
        return "L" + ClassNames.binaryClassName(type) + ";";
    }

    public static String annotationTypeName(Class<? extends Annotation> annotationType) {
        return referenceFieldTypeDescriptor(annotationType);
    }

    public static String referenceFieldTypeDescriptorToClassName(String fieldDescriptor) {
        if (fieldDescriptor.length() < 3 || fieldDescriptor.charAt(0) != 'L'
                || fieldDescriptor.charAt(fieldDescriptor.length() - 1) != ';') {
            throw new IllegalArgumentException(
                    "Invalid format for field descriptor " + fieldDescriptor);
        }
        return fieldDescriptor.substring(1, fieldDescriptor.length() - 1).replace('/', '.');
    }

    public static Optional<AnnotationEntry> getAnnotation(
            Method method, Class<? extends Annotation> annotationType) {
        return Arrays.stream(method.getAnnotationEntries())
                .filter(ae -> ae.getAnnotationType().equals(annotationTypeName(annotationType)))
                .findFirst();
    }
}
