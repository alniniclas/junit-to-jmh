package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AstEqualityTest {
    private static final AstResourceLoader astLoader = new AstResourceLoader(AstEqualityTest.class);

    @Test
    public void astEqualsItself() throws IOException {
        CompilationUnit helloWorld = astLoader.load("HelloWorld.java");
        CompilationUnit helloWorld2 = astLoader.load("HelloWorld.java");

        assertEquals(helloWorld, helloWorld2);
    }

    @Test
    public void differingAstsAreNotEqual() throws IOException {
        CompilationUnit helloWorld = astLoader.load("HelloWorld.java");
        CompilationUnit helloWord = astLoader.load("HelloWorld_HelloWord.java");

        assertNotEquals(helloWorld, helloWord);
    }

    @Test
    public void astEqualityIgnoresWhitespace() throws IOException {
        CompilationUnit helloWorld = astLoader.load("HelloWorld.java");
        CompilationUnit helloWorldOneLine = astLoader.load("HelloWorld_Oneliner.java");

        assertEquals(helloWorld, helloWorldOneLine);
    }

    @Test
    public void astEqualityDoesNotIgnoreComments() throws IOException {
        CompilationUnit helloWorld = astLoader.load("HelloWorld.java");
        CompilationUnit helloWorldCommented = astLoader.load("HelloWorld_Comment.java");

        assertNotEquals(helloWorld, helloWorldCommented);
    }
}
