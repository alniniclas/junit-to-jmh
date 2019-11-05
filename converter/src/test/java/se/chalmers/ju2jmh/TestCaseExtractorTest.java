package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCaseExtractorTest {
    private static CompilationUnit loadAst(String resource) throws IOException {
        return AstResources.loadAst(TestCaseExtractorTest.class, resource);
    }

    @Test
    public void extractsTestCase() throws IOException {
        CompilationUnit input = loadAst("SimpleTest.java");
        CompilationUnit expected = loadAst("SimpleTest_Extracted.java");

        CompilationUnit output = TestCaseExtractor.extractTestMethods(input);

        assertEquals(expected, output);
    }

    @Test
    public void doesNotModifyInput() throws IOException {
        CompilationUnit input = loadAst("SimpleTest.java");
        CompilationUnit expected = loadAst("SimpleTest.java");

        TestCaseExtractor.extractTestMethods(input);

        assertEquals(expected, input);
    }

    @Test
    public void handlesDifferentAnnotations() throws IOException {
        CompilationUnit input = loadAst("DifferentAnnotations.java");
        CompilationUnit expected = loadAst("DifferentAnnotations.java");

        CompilationUnit output = TestCaseExtractor.extractTestMethods(input);

        assertEquals(expected, output);
    }
}
