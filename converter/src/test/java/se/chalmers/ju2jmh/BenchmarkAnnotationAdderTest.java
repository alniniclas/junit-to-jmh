package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BenchmarkAnnotationAdderTest {
    private static CompilationUnit loadAst(String resource) throws IOException {
        return AstResources.loadAst(BenchmarkAnnotationAdderTest.class, resource);
    }

    @Test
    public void addsBenchmarkAnnotations() throws IOException {
        CompilationUnit ast = loadAst("SimpleTest_Input.java");
        CompilationUnit expected = loadAst("SimpleTest_Expected.java");

        BenchmarkAnnotationAdder.addBenchmarkAnnotations(ast);

        assertEquals(expected, ast);
    }

    @Test
    public void modifiesInput() throws IOException {
        CompilationUnit input = loadAst("SimpleTest_Input.java");
        CompilationUnit unexpected = loadAst("SimpleTest_Input.java");

        BenchmarkAnnotationAdder.addBenchmarkAnnotations(input);

        assertNotEquals(unexpected, input);
    }

    @Test
    public void handlesDifferentAnnotations() throws IOException {
        CompilationUnit ast = loadAst("DifferentAnnotations_Input.java");
        CompilationUnit expected = loadAst("DifferentAnnotations_Expected.java");

        BenchmarkAnnotationAdder.addBenchmarkAnnotations(ast);

        assertEquals(expected, ast);
    }
}
