package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AstEqualityTest {
    private static CompilationUnit loadAst(String resource) throws IOException {
        return AstResources.loadAst(AstEqualityTest.class, resource);
    }

    @Test
    public void astEqualsItself() throws IOException {
        CompilationUnit helloWorld = loadAst("HelloWorld.java");
        CompilationUnit helloWorld2 = loadAst("HelloWorld.java");

        assertEquals(helloWorld, helloWorld2);
    }

    @Test
    public void differingAstsAreNotEqual() throws IOException {
        CompilationUnit helloWorld = loadAst("HelloWorld.java");
        CompilationUnit helloWord = loadAst("HelloWorld_HelloWord.java");

        assertNotEquals(helloWorld, helloWord);
    }

    @Test
    public void astEqualityIgnoresWhitespace() throws IOException {
        CompilationUnit helloWorld = loadAst("HelloWorld.java");
        CompilationUnit helloWorldOneLine = loadAst("HelloWorld_Oneliner.java");

        assertEquals(helloWorld, helloWorldOneLine);
    }

    @Test
    public void astEqualityDoesNotIgnoreComments() throws IOException {
        CompilationUnit helloWorld = loadAst("HelloWorld.java");
        CompilationUnit helloWorldCommented = loadAst("HelloWorld_Comment.java");

        assertNotEquals(helloWorld, helloWorldCommented);
    }
}
