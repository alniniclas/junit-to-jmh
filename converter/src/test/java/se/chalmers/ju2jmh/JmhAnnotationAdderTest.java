package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static se.chalmers.ju2jmh.AstMatcher.equalsAst;

public class JmhAnnotationAdderTest {
    private static final AstResourceLoader astLoader =
            new AstResourceLoader(JmhAnnotationAdderTest.class);

    @ParameterizedTest
    @ValueSource(strings = {
            "SimpleTest",
            "FixtureMethods",
            "DifferentAnnotations",
            "MultipleClasses",
            "IgnoredTest"
    })
    public void producesExpectedOutput(String testCase) throws IOException {
        CompilationUnit ast = astLoader.load(testCase + "_Input.java");
        CompilationUnit expected = astLoader.load(testCase + "_Expected.java");

        JmhAnnotationAdder.addBenchmarkAnnotations(ast);

        assertThat(ast, equalsAst(expected));
    }
}
