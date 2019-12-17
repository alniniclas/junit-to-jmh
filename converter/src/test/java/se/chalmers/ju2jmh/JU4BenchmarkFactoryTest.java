package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.testinput.SimpleClass;
import se.chalmers.ju2jmh.testinput.unittests.ClassWithNestedTests;
import se.chalmers.ju2jmh.testinput.unittests.SimpleUnitTest;
import se.chalmers.ju2jmh.testinput.unittests.TestAbstractClass;
import se.chalmers.ju2jmh.testinput.unittests.TestImplementation;
import se.chalmers.ju2jmh.testinput.unittests.TestImplementationSubclass;
import se.chalmers.ju2jmh.testinput.unittests.TestInterface;
import se.chalmers.ju2jmh.testinput.unittests.TestOverridingImplementation;
import se.chalmers.ju2jmh.testinput.unittests.TwoTestCases;

import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.chalmers.ju2jmh.AstMatcher.equalsAst;

public class JU4BenchmarkFactoryTest {
    private static final AstResourceLoader astLoader =
            new AstResourceLoader(JU4BenchmarkFactoryTest.class);
    private static InputClassRepository repository;

    @BeforeAll
    public static void setUpRepository(@TempDir Path tempDir)
            throws IOException, ClassNotFoundException {
        InputClassDirectory inputClassDirectory = InputClassDirectory.directoryWithClasses(
                tempDir, SimpleUnitTest.class, TwoTestCases.class, TestImplementation.class,
                TestAbstractClass.class, TestInterface.class, TestImplementationSubclass.class,
                TestOverridingImplementation.class, ClassWithNestedTests.class,
                ClassWithNestedTests.Nested.class, ClassWithNestedTests.Nested.NestedNested.class,
                SimpleClass.class);
        repository = new InputClassRepository(inputClassDirectory.sourcesDirectory(),
                inputClassDirectory.bytecodeDirectory());
    }

    public void assertProducesExpectedOutput(Class<?> clazz)
            throws IOException, ClassNotFoundException, InvalidInputClassException {
        JU4BenchmarkFactory benchmarkFactory = new JU4BenchmarkFactory(repository);
        CompilationUnit expected = astLoader.load(
                ClassNames.shortClassName(clazz).replace('$', '_') + "_Expected.java");
        CompilationUnit generated = benchmarkFactory.createBenchmarkFromTest(clazz.getName());
        assertThat(generated, equalsAst(expected));
    }

    @Test
    public void producesBenchmarkFromSimpleUnitTest()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        assertProducesExpectedOutput(SimpleUnitTest.class);
    }

    @Test
    public void canHandleMultipleMethods()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        assertProducesExpectedOutput(TwoTestCases.class);
    }

    @Test
    public void includesMethodsFromSuperclassButNotInterfaces()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        assertProducesExpectedOutput(TestImplementation.class);
    }

    @Test
    public void includesMethodsFromSuperclassOfSuperclass()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        assertProducesExpectedOutput(TestImplementationSubclass.class);
    }

    @Test
    public void doesNotProduceDuplicateMethods()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        assertProducesExpectedOutput(TestOverridingImplementation.class);
    }

    @Test
    public void canProduceBenchmarksFromNestedClasses()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        assertProducesExpectedOutput(ClassWithNestedTests.Nested.class);
        assertProducesExpectedOutput(ClassWithNestedTests.Nested.NestedNested.class);
    }

    @Test
    public void throwsExceptionWhenConvertingAbstractClass() {
        JU4BenchmarkFactory benchmarkFactory = new JU4BenchmarkFactory(repository);

        assertThrows(InvalidInputClassException.class,
                () -> benchmarkFactory.createBenchmarkFromTest(TestAbstractClass.class.getName()));
    }

    @Test
    public void throwsExceptionWhenConvertingInterface() {
        JU4BenchmarkFactory benchmarkFactory = new JU4BenchmarkFactory(repository);

        assertThrows(InvalidInputClassException.class,
                () -> benchmarkFactory.createBenchmarkFromTest(TestInterface.class.getName()));
    }

    @Test
    public void throwsExceptionWhenConvertingNonTestClass() {
        JU4BenchmarkFactory benchmarkFactory = new JU4BenchmarkFactory(repository);

        assertThrows(InvalidInputClassException.class,
                () -> benchmarkFactory.createBenchmarkFromTest(SimpleClass.class.getName()));
    }
}
