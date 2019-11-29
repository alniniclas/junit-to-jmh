package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionTestRestructurerTest {
    private static final AstResourceLoader astLoader =
            new AstResourceLoader(ExceptionTestRestructurerTest.class);

    @Test
    public void transformsExceptionTestsCorrectly() throws IOException {
        CompilationUnit ast = astLoader.load("ExceptionTests_Input.java");
        CompilationUnit expected = astLoader.load("ExceptionTests_Expected.java");

        ExceptionTestRestructurer.restructureExceptionTests(ast);

        assertEquals(expected, StaticJavaParser.parse(ast.toString()));
    }
}
