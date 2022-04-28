package se.chalmers.ju2jmh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.model.UnitTestClass;
import se.chalmers.ju2jmh.testinput.unittests.ExceptionTest;
import se.chalmers.ju2jmh.testinput.unittests.IgnoredUnitTest;
import se.chalmers.ju2jmh.testinput.unittests.NoExceptionTest;
import se.chalmers.ju2jmh.testinput.unittests.SimpleUnitTest;
import se.chalmers.ju2jmh.testinput.unittests.TestAbstractClass;
import se.chalmers.ju2jmh.testinput.unittests.TestImplementation;
import se.chalmers.ju2jmh.testinput.unittests.TestImplementationSubclass;
import se.chalmers.ju2jmh.testinput.unittests.UnitTestWithFixtureMethodsAndRules;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnitTestClassRepositoryTest {
    private InputClassDirectory inputClassDirectory;
    private UnitTestClassRepository repository;

    @BeforeEach
    public void setUpRepository(@TempDir Path tempDir)
            throws IOException, ClassNotFoundException {
        inputClassDirectory = InputClassDirectory.directoryWithClasses(tempDir);
        repository = new UnitTestClassRepository(
                new InputClassRepository(
                        inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory()));
    }

    @Test
    public void parsesSimpleUnitTest() throws IOException, ClassNotFoundException {
        Class<?> testClass = SimpleUnitTest.class;
        inputClassDirectory.add(testClass);
        UnitTestClass expected = UnitTestClass.Builder.forClass(testClass.getCanonicalName())
                .withTest("test")
                .build();

        UnitTestClass actual = repository.findClass(testClass.getCanonicalName());

        assertEquals(expected, actual);
    }

    @Test
    public void parsesExceptionTest() throws IOException, ClassNotFoundException {
        Class<?> testClass = ExceptionTest.class;
        inputClassDirectory.add(testClass);
        UnitTestClass expected = UnitTestClass.Builder.forClass(testClass.getCanonicalName())
                .withExceptionTest("testException", Exception.class.getCanonicalName())
                .build();

        UnitTestClass actual = repository.findClass(testClass.getCanonicalName());

        assertEquals(expected, actual);
    }

    @Test
    public void parsesFixtureMethodsAndRules() throws IOException, ClassNotFoundException {
        Class<?> testClass = UnitTestWithFixtureMethodsAndRules.class;
        inputClassDirectory.add(testClass);
        UnitTestClass expected = UnitTestClass.Builder.forClass(testClass.getCanonicalName())
                .withBeforeClass("beforeClass1")
                .withBeforeClass("beforeClass2")
                .withAfterClass("afterClass1")
                .withAfterClass("afterClass2")
                .withClassRuleField("classRuleField1")
                .withClassRuleField("classRuleField2")
                .withClassRuleMethod("classRuleMethod1")
                .withClassRuleMethod("classRuleMethod2")
                .withBefore("before1")
                .withBefore("before2")
                .withAfter("after1")
                .withAfter("after2")
                .withInstanceRuleField("ruleField1")
                .withInstanceRuleField("ruleField2")
                .withInstanceRuleMethod("ruleMethod1")
                .withInstanceRuleMethod("ruleMethod2")
                .withTest("test")
                .build();

        UnitTestClass actual = repository.findClass(testClass.getCanonicalName());

        assertEquals(expected, actual);
    }

    @Test
    public void parsesSuperclass() throws IOException, ClassNotFoundException {
        Class<?> testSuperSuperclass = TestAbstractClass.class;
        Class<?> testSuperclass = TestImplementation.class;
        Class<?> testClass = TestImplementationSubclass.class;
        inputClassDirectory.add(testSuperSuperclass);
        inputClassDirectory.add(testSuperclass);
        inputClassDirectory.add(testClass);
        UnitTestClass expectedSuperSuper =
                UnitTestClass.Builder.forClass(testSuperSuperclass.getCanonicalName())
                        .withTest("abstractClassTest")
                        .build();
        UnitTestClass expectedSuper =
                UnitTestClass.Builder.forClass(testSuperclass.getCanonicalName())
                        .withSuperclass(expectedSuperSuper)
                        .withTest("implementationTest")
                        .build();
        UnitTestClass expected = UnitTestClass.Builder.forClass(testClass.getCanonicalName())
                .withSuperclass(expectedSuper)
                .withTest("subclassTest")
                .build();

        UnitTestClass actual = repository.findClass(testClass.getCanonicalName());

        assertEquals(expected, actual);
    }

    @Test
    public void handlesMissingSuperclass() throws IOException, ClassNotFoundException {
        Class<?> testSuperclass = TestImplementation.class;
        Class<?> testClass = TestImplementationSubclass.class;
        inputClassDirectory.add(testClass);
        UnitTestClass expectedSuper =
                UnitTestClass.Builder.forClass(testSuperclass.getCanonicalName())
                        .build();
        UnitTestClass expected = UnitTestClass.Builder.forClass(testClass.getCanonicalName())
                .withSuperclass(expectedSuper)
                .withTest("subclassTest")
                .build();

        UnitTestClass actual = repository.findClass(testClass.getCanonicalName());

        assertEquals(expected, actual);
    }

    @Test
    public void skipsIgnoredUnitTest() throws IOException, ClassNotFoundException {
        Class<?> testClass = IgnoredUnitTest.class;
        inputClassDirectory.add(testClass);
        UnitTestClass expected = UnitTestClass.Builder.forClass(testClass.getCanonicalName())
                .build();

        UnitTestClass actual = repository.findClass(testClass.getCanonicalName());

        assertEquals(expected, actual);
    }

    @Test
    public void skipsDefaultExpectedException() throws IOException, ClassNotFoundException {
        Class<?> testClass = NoExceptionTest.class;
        inputClassDirectory.add(testClass);
        UnitTestClass expected = UnitTestClass.Builder.forClass(testClass.getCanonicalName())
                .withTest("test")
                .build();

        UnitTestClass actual = repository.findClass(testClass.getCanonicalName());

        assertEquals(expected, actual);
    }
}
