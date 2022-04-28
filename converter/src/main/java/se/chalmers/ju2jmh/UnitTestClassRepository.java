package se.chalmers.ju2jmh;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import se.chalmers.ju2jmh.model.UnitTestClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A repository for finding and loading {@link UnitTestClass}es.
 */
public class UnitTestClassRepository {
    private static final String TEST_ANNOTATION = Bytecode.annotationTypeName(Test.class);
    private static final String IGNORE_ANNOTATION = Bytecode.annotationTypeName(Ignore.class);
    private static final String BEFORE_ANNOTATION = Bytecode.annotationTypeName(Before.class);
    private static final String AFTER_ANNOTATION = Bytecode.annotationTypeName(After.class);
    private static final String BEFORE_CLASS_ANNOTATION =
            Bytecode.annotationTypeName(BeforeClass.class);
    private static final String AFTER_CLASS_ANNOTATION =
            Bytecode.annotationTypeName(AfterClass.class);
    private static final String RULE_ANNOTATION = Bytecode.annotationTypeName(Rule.class);
    private static final String CLASS_RULE_ANNOTATION =
            Bytecode.annotationTypeName(ClassRule.class);

    private final Map<String, UnitTestClass> knownClasses = new HashMap<>();
    private final InputClassRepository repository;

    /**
     * Creates a new unit test class repository, loading source code and byte code from the given
     * {@link InputClassRepository}.
     *
     * @param repository input class repository to load source code and byte code from
     */
    public UnitTestClassRepository(InputClassRepository repository) {
        this.repository = repository;
    }

    private UnitTestClass loadFromRepository(String name) throws ClassNotFoundException {
        InputClass testInputClass = repository.findClass(name);
        UnitTestClass.Builder builder = UnitTestClass.Builder.forClass(name);
        String superclassName = testInputClass.getSuperclassName();
        if (!superclassName.equals(Object.class.getCanonicalName())) {
            try {
                builder.withSuperclass(findClass(superclassName));
            } catch (ClassNotFoundException e) {
                // Superclass unavailable. Assume no tests, fixture methods, rules, or superclasses
                // are present.
                UnitTestClass superclass = UnitTestClass.Builder.forClass(superclassName).build();
                knownClasses.put(superclassName, superclass);
                builder.withSuperclass(superclass);
            }
        }
        JavaClass bytecode = testInputClass.getBytecode();
        for (Method method : bytecode.getMethods()) {
            if (method.getArgumentTypes().length > 0) {
                continue;
            }
            for (AnnotationEntry annotation : method.getAnnotationEntries()) {
                String annotationType = annotation.getAnnotationType();
                if (annotationType.equals(TEST_ANNOTATION)) {
                    if (Arrays.stream(method.getAnnotationEntries())
                            .map(AnnotationEntry::getAnnotationType)
                            .anyMatch(Predicate.isEqual(IGNORE_ANNOTATION))) {
                        continue;
                    }
                    Optional<String> expected = Arrays.stream(annotation.getElementValuePairs())
                            .filter(evp -> evp.getNameString().equals("expected"))
                            .map(ElementValuePair::getValue)
                            .map(ElementValue::stringifyValue)
                            .map(Bytecode::referenceFieldTypeDescriptorToClassName)
                            .filter(Predicate.not(Predicate.isEqual(Test.None.class.getName())))
                            .findFirst();
                    if (expected.isEmpty()) {
                        builder.withTest(method.getName());
                    } else {
                        builder.withExceptionTest(method.getName(), expected.get());
                    }
                } else if (annotationType.equals(BEFORE_ANNOTATION)) {
                    builder.withBefore(method.getName());
                } else if (annotationType.equals(AFTER_ANNOTATION)) {
                    builder.withAfter(method.getName());
                } else if (annotationType.equals(BEFORE_CLASS_ANNOTATION)) {
                    builder.withBeforeClass(method.getName());
                } else if (annotationType.equals(AFTER_CLASS_ANNOTATION)) {
                    builder.withAfterClass(method.getName());
                } else if (annotationType.equals(RULE_ANNOTATION)) {
                    builder.withInstanceRuleMethod(method.getName());
                } else if (annotationType.equals(CLASS_RULE_ANNOTATION)) {
                    builder.withClassRuleMethod(method.getName());
                }
            }
        }
        for (Field field : bytecode.getFields()) {
            for (AnnotationEntry annotation : field.getAnnotationEntries()) {
                String annotationType = annotation.getAnnotationType();
                if (annotationType.equals(RULE_ANNOTATION)) {
                    builder.withInstanceRuleField(field.getName());
                } else if (annotationType.equals(CLASS_RULE_ANNOTATION)) {
                    builder.withClassRuleField(field.getName());
                }
            }
        }
        return builder.build();
    }

    /**
     * Returns a {@link UnitTestClass} corresponding to the class with the given name, if present in
     * the underlying repository.
     *
     * @param name the fully qualified name of the unit test class
     * @return a unit test class representing the test class with the given name
     * @throws ClassNotFoundException if source code or byte code for the given class was not
     *     available
     */
    public UnitTestClass findClass(String name) throws ClassNotFoundException {
        UnitTestClass testClass = knownClasses.get(name);
        if (testClass != null) {
            return testClass;
        }
        testClass = loadFromRepository(name);
        knownClasses.put(name, testClass);
        return testClass;
    }
}
