package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.testinput.unittests.*;

import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static se.chalmers.ju2jmh.AstMatcher.equalsAst;

public class JU4BenchmarkFactoryTest {
    private static final AstResourceLoader astLoader =
            new AstResourceLoader(JU4BenchmarkFactoryTest.class);
    private static SourceClassRepository repository;

    @BeforeAll
    public static void setUpRepository(@TempDir Path tempDir)
            throws IOException, ClassNotFoundException {
        SourceClassDirectory sourceClassDirectory = SourceClassDirectory.directoryWithClasses(
                tempDir, SimpleUnitTest.class, TwoTestCases.class, TestImplementation.class,
                TestAbstractClass.class, TestInterface.class, TestImplementationSubclass.class,
                TestOverridingImplementation.class, ClassWithNestedTests.class,
                ClassWithNestedTests.Nested.class, ClassWithNestedTests.Nested.NestedNested.class);
        repository = new SourceClassRepository(sourceClassDirectory.sourcesDirectory().toString(),
                sourceClassDirectory.bytecodeDirectory().toString());
    }

    private static String shortClassName(Class<?> clazz) {
        String className = clazz.getName();
        return clazz.getName().substring(className.lastIndexOf('.') + 1).replace('$', '.');
    }

    public void assertProducesExpectedOutput(Class<?> clazz)
            throws IOException, ClassNotFoundException {
        JU4BenchmarkFactory benchmarkFactory = new JU4BenchmarkFactory(repository);
        CompilationUnit expected = astLoader.load(
                shortClassName(clazz).replace('.', '_') + "_Expected.java");
        CompilationUnit generated = benchmarkFactory.createBenchmarkFromTest(clazz.getName());
        assertThat(generated, equalsAst(expected));
    }

    @Test
    public void producesBenchmarkFromSimpleUnitTest() throws ClassNotFoundException, IOException {
        assertProducesExpectedOutput(SimpleUnitTest.class);
    }

    @Test
    public void canHandleMultipleMethods() throws ClassNotFoundException, IOException {
        assertProducesExpectedOutput(TwoTestCases.class);
    }

    @Test
    public void includesMethodsFromSuperclassButNotInterfaces()
            throws ClassNotFoundException, IOException {
        assertProducesExpectedOutput(TestImplementation.class);
    }

    @Test
    public void includesMethodsFromSuperclassOfSuperclass()
            throws ClassNotFoundException, IOException {
        assertProducesExpectedOutput(TestImplementationSubclass.class);
    }

    @Test
    public void doesNotProduceDuplicateMethods() throws ClassNotFoundException, IOException {
        assertProducesExpectedOutput(TestOverridingImplementation.class);
    }

    @Test
    public void canProduceBenchmarksFromNestedClasses()
            throws ClassNotFoundException, IOException {
        assertProducesExpectedOutput(ClassWithNestedTests.Nested.class);
        assertProducesExpectedOutput(ClassWithNestedTests.Nested.NestedNested.class);
    }
}
