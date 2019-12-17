package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.testinput.SimpleClass;
import se.chalmers.ju2jmh.testinput.SimpleSubclass;
import se.chalmers.ju2jmh.testinput.SimpleSuperclass;
import se.chalmers.ju2jmh.testinput.unittests.ClassWithNestedTestSubclasses;
import se.chalmers.ju2jmh.testinput.unittests.ClassWithNestedTests;
import se.chalmers.ju2jmh.testinput.unittests.ClassWithOnlyFixtureMethods;
import se.chalmers.ju2jmh.testinput.unittests.ExceptionTest;
import se.chalmers.ju2jmh.testinput.unittests.SimpleUnitTest;
import se.chalmers.ju2jmh.testinput.unittests.TestAbstractClass;
import se.chalmers.ju2jmh.testinput.unittests.TestImplementation;
import se.chalmers.ju2jmh.testinput.unittests.TestImplementationSubclass;
import se.chalmers.ju2jmh.testinput.unittests.TestInterface;
import se.chalmers.ju2jmh.testinput.unittests.TestSubclassWithoutOwnTests;
import se.chalmers.ju2jmh.testinput.unittests.TestWithNestedTypesNamedBenchmark;
import se.chalmers.ju2jmh.testinput.unittests.TwoTestCases;
import se.chalmers.ju2jmh.testinput.unittests.UnitTestWithFixtureMethodsAndRules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.chalmers.ju2jmh.AstMatcher.equalsAst;

public class NestedBenchmarkSuiteBuilderTest {
    private static final AstResourceLoader astLoader =
            new AstResourceLoader(NestedBenchmarkSuiteBuilderTest.class);
    private InputClassDirectory inputClassDirectory;

    private static CompilationUnit originalAst(Class<?> clazz, InputClassDirectory directory) throws IOException {
        String enclosingClassName = ClassNames.outermostClassName(clazz);
        Path file = directory.sourcesDirectory()
                .resolve(enclosingClassName.replace('.', File.separatorChar) + ".java");
        return StaticJavaParser.parse(file);
    }

    private CompilationUnit originalAst(Class<?> clazz) throws IOException {
        return originalAst(clazz, inputClassDirectory);
    }

    @BeforeEach
    public void setUpRepository(@TempDir Path tempDir) {
        inputClassDirectory = new InputClassDirectory(tempDir);
    }

    @Test
    public void canConvertSimpleUnitTest() throws ClassNotFoundException, IOException {
        inputClassDirectory.add(SimpleUnitTest.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(SimpleUnitTest.class.getName(),
                astLoader.load("SimpleUnitTest_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(SimpleUnitTest.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void includesAllTestCases() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(TwoTestCases.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(TwoTestCases.class.getName(),
                astLoader.load("TwoTestCases_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(TwoTestCases.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void canConvertUnitTestWithFixtureMethodsAndRules()
            throws ClassNotFoundException, IOException {
        inputClassDirectory.add(UnitTestWithFixtureMethodsAndRules.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(UnitTestWithFixtureMethodsAndRules.class.getName(),
                astLoader.load("UnitTestWithFixtureMethodsAndRules_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(UnitTestWithFixtureMethodsAndRules.class.getName())
                        .buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void includesPresentSuperclass() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(TestInterface.class);
        inputClassDirectory.add(TestAbstractClass.class);
        inputClassDirectory.add(TestImplementation.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(TestInterface.class.getName(), originalAst(TestInterface.class));
        expected.put(TestAbstractClass.class.getName(),
                astLoader.load("TestAbstractClass_Expected.java"));
        expected.put(TestImplementation.class.getName(),
                astLoader.load("TestImplementation_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(TestImplementation.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void excludesAbsentSuperclass() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(TestImplementation.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(TestImplementation.class.getName(),
                astLoader.load("TestImplementation_Expected_NoSuperclass.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(TestImplementation.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void includesAllPresentSuperclasses() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(TestInterface.class);
        inputClassDirectory.add(TestAbstractClass.class);
        inputClassDirectory.add(TestImplementation.class);
        inputClassDirectory.add(TestImplementationSubclass.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(TestInterface.class.getName(), originalAst(TestInterface.class));
        expected.put(TestAbstractClass.class.getName(),
                astLoader.load("TestAbstractClass_Expected.java"));
        expected.put(TestImplementation.class.getName(),
                astLoader.load("TestImplementation_Expected_Abstract.java"));
        expected.put(TestImplementationSubclass.class.getName(),
                astLoader.load("TestImplementationSubclass_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(TestImplementationSubclass.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void addingWithNestedIncludesNestedTests() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(ClassWithNestedTests.class);
        inputClassDirectory.add(ClassWithNestedTests.Nested.class);
        inputClassDirectory.add(ClassWithNestedTests.Nested.NestedNested.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(ClassWithNestedTests.class.getName(),
                astLoader.load("ClassWithNestedTests_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClassIncludingNested(ClassWithNestedTests.class.getName())
                        .buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void addingWithoutNestedExcludesNestedTests()
            throws IOException, ClassNotFoundException {
        inputClassDirectory.add(ClassWithNestedTests.class);
        inputClassDirectory.add(ClassWithNestedTests.Nested.class);
        inputClassDirectory.add(ClassWithNestedTests.Nested.NestedNested.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(ClassWithNestedTests.class.getName(), originalAst(ClassWithNestedTests.class));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(ClassWithNestedTests.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void canHandleInheritanceOfNestedTests() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(ClassWithNestedTests.class);
        inputClassDirectory.add(ClassWithNestedTests.Nested.class);
        inputClassDirectory.add(ClassWithNestedTests.Nested.NestedNested.class);
        inputClassDirectory.add(ClassWithNestedTestSubclasses.class);
        inputClassDirectory.add(ClassWithNestedTestSubclasses.NestedSubclass.class);
        inputClassDirectory.add(
                ClassWithNestedTestSubclasses.NestedSubclass.NestedNestedSubclass.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(ClassWithNestedTests.class.getName(),
                astLoader.load("ClassWithNestedTests_Expected_Abstract.java"));
        expected.put(ClassWithNestedTestSubclasses.class.getName(),
                astLoader.load("ClassWithNestedTestSubclasses_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClassIncludingNested(ClassWithNestedTestSubclasses.class.getName())
                        .buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void canHandleNamingConflicts() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(TestWithNestedTypesNamedBenchmark.class);
        inputClassDirectory.add(TestWithNestedTypesNamedBenchmark._Benchmark.class);
        inputClassDirectory.add(TestWithNestedTypesNamedBenchmark._Benchmark_0.class);
        inputClassDirectory.add(TestWithNestedTypesNamedBenchmark._Benchmark_1.class);
        inputClassDirectory.add(TestWithNestedTypesNamedBenchmark._Benchmark_2.class);
        inputClassDirectory.add(TestWithNestedTypesNamedBenchmark._Benchmark_3.class);
        inputClassDirectory.add(TestWithNestedTypesNamedBenchmark._Benchmark.Nested.class);
        inputClassDirectory.add(
                TestWithNestedTypesNamedBenchmark._Benchmark.Nested._Benchmark_0.class);
        inputClassDirectory.add(
                TestWithNestedTypesNamedBenchmark._Benchmark.Nested.NestedNested.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(TestWithNestedTypesNamedBenchmark.class.getName(),
                astLoader.load("TestWithNestedTypesNamedBenchmark_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClassIncludingNested(
                        TestWithNestedTypesNamedBenchmark.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void doesNotModifyNonTests() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(SimpleClass.class);
        inputClassDirectory.add(SimpleSuperclass.class);
        inputClassDirectory.add(SimpleSubclass.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(SimpleClass.class.getName(), originalAst(SimpleClass.class));
        expected.put(SimpleSuperclass.class.getName(), originalAst(SimpleSuperclass.class));
        expected.put(SimpleSubclass.class.getName(), originalAst(SimpleSubclass.class));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite = builder.addTestClass(SimpleClass.class.getName())
                .addTestClass(SimpleSuperclass.class.getName())
                .addTestClass(SimpleSubclass.class.getName())
                .buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void includesButDoesNotCreateBenchmarkForNonAddedTest()
            throws IOException, ClassNotFoundException {
        inputClassDirectory.add(SimpleUnitTest.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(SimpleUnitTest.class.getName(), originalAst(SimpleUnitTest.class));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite = builder.buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void doesNotCreateBenchmarkForInterface() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(TestInterface.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(TestInterface.class.getName(), originalAst(TestInterface.class));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(TestInterface.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void canHandleExpectedExceptions() throws IOException, ClassNotFoundException {
        inputClassDirectory.add(ExceptionTest.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(ExceptionTest.class.getName(),
                astLoader.load("ExceptionTest_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(ExceptionTest.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void createsBenchmarkForClassWithOnlyInheritedTestMembers()
            throws IOException, ClassNotFoundException {
        inputClassDirectory.add(TestAbstractClass.class);
        inputClassDirectory.add(TestSubclassWithoutOwnTests.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(TestAbstractClass.class.getName(),
                astLoader.load("TestAbstractClass_Expected.java"));
        expected.put(TestSubclassWithoutOwnTests.class.getName(),
                astLoader.load("TestSubclassWithoutOwnTests_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(TestSubclassWithoutOwnTests.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void createsBenchmarkForClassWithOnlyFixtureMethods()
            throws IOException, ClassNotFoundException {
        inputClassDirectory.add(ClassWithOnlyFixtureMethods.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(ClassWithOnlyFixtureMethods.class.getName(),
                astLoader.load("ClassWithOnlyFixtureMethods_Expected.java"));
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(inputClassDirectory.sourcesDirectory(),
                        inputClassDirectory.bytecodeDirectory());

        Map<String, CompilationUnit> suite =
                builder.addTestClass(ClassWithOnlyFixtureMethods.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }

    @Test
    public void includesClassesFromSeparateDirectories(@TempDir Path tempDir)
            throws IOException, ClassNotFoundException {
        InputClassDirectory dir1 = new InputClassDirectory(tempDir.resolve("dir1"));
        InputClassDirectory dir2 = new InputClassDirectory(tempDir.resolve("dir2"));
        InputClassDirectory dir3 = new InputClassDirectory(tempDir.resolve("dir3"));
        InputClassDirectory dir4 = new InputClassDirectory(tempDir.resolve("dir4"));
        InputClassDirectory dir5 = new InputClassDirectory(tempDir.resolve("dir5"));
        InputClassDirectory dir6 = new InputClassDirectory(tempDir.resolve("dir5"));
        dir1.addSource(TestInterface.class);
        dir2.addSource(TestAbstractClass.class);
        dir3.addSource(TestImplementation.class);
        dir4.addBytecode(TestImplementation.class);
        dir5.addBytecode(TestAbstractClass.class);
        dir6.addBytecode(TestInterface.class);
        Map<String, CompilationUnit> expected = new HashMap<>();
        expected.put(TestInterface.class.getName(), originalAst(TestInterface.class, dir1));
        expected.put(TestAbstractClass.class.getName(),
                astLoader.load("TestAbstractClass_Expected.java"));
        expected.put(TestImplementation.class.getName(),
                astLoader.load("TestImplementation_Expected.java"));
        List<Path> sourcePaths = List.of(dir1.sourcesDirectory(), dir2.sourcesDirectory(),
                dir3.sourcesDirectory());
        List<Path> classPath = List.of(dir4.bytecodeDirectory(), dir5.bytecodeDirectory(),
                dir6.bytecodeDirectory());
        NestedBenchmarkSuiteBuilder builder =
                new NestedBenchmarkSuiteBuilder(sourcePaths, classPath);

        Map<String, CompilationUnit> suite =
                builder.addTestClass(TestImplementation.class.getName()).buildSuite();

        assertEquals(expected.keySet(), suite.keySet());
        for (String compilationUnit : expected.keySet()) {
            assertThat(suite.get(compilationUnit), equalsAst(expected.get(compilationUnit)));
        }
    }
}
