package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BenchmarkAnnotationAdderTest {
    private static CompilationUnit loadAst(String resource) throws IOException {
        return AstResources.loadAst(BenchmarkAnnotationAdderTest.class, resource);
    }

    @Test
    public void addsBenchmarkAnnotations() throws IOException {
        CompilationUnit input = loadAst("SimpleTest_Input.java");
        CompilationUnit expected = loadAst("SimpleTest_Expected.java");

        CompilationUnit output = BenchmarkAnnotationAdder.addBenchmarkAnnotations(input);

        assertEquals(expected, output);
    }

    @Test
    public void doesNotModifyInput() throws IOException {
        CompilationUnit input = loadAst("SimpleTest_Input.java");
        CompilationUnit expected = loadAst("SimpleTest_Input.java");

        BenchmarkAnnotationAdder.addBenchmarkAnnotations(input);

        assertEquals(expected, input);
    }
}
