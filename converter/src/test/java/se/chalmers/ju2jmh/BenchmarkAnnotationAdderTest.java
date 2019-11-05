package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BenchmarkAnnotationAdderTest {
    private static final AstResourceLoader astLoader =
            new AstResourceLoader(BenchmarkAnnotationAdderTest.class);

    @Test
    public void addsBenchmarkAnnotations() throws IOException {
        CompilationUnit ast = astLoader.load("SimpleTest_Input.java");
        CompilationUnit expected = astLoader.load("SimpleTest_Expected.java");

        BenchmarkAnnotationAdder.addBenchmarkAnnotations(ast);

        assertEquals(expected, ast);
    }

    @Test
    public void modifiesInput() throws IOException {
        CompilationUnit input = astLoader.load("SimpleTest_Input.java");
        CompilationUnit unexpected = astLoader.load("SimpleTest_Input.java");

        BenchmarkAnnotationAdder.addBenchmarkAnnotations(input);

        assertNotEquals(unexpected, input);
    }

    @Test
    public void handlesDifferentAnnotations() throws IOException {
        CompilationUnit ast = astLoader.load("DifferentAnnotations_Input.java");
        CompilationUnit expected = astLoader.load("DifferentAnnotations_Expected.java");

        BenchmarkAnnotationAdder.addBenchmarkAnnotations(ast);

        assertEquals(expected, ast);
    }
}
