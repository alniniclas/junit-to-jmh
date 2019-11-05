package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AstEqualityTest {
    private static final String RESOURCES_ROOT =
            AstEqualityTest.class.getCanonicalName().replace('.', '/');

    private static CompilationUnit parseResource(String resource) throws IOException {
        return StaticJavaParser.parseResource(RESOURCES_ROOT + '/' + resource);
    }

    @Test
    public void astEqualsItself() throws IOException {
        CompilationUnit helloWorld = parseResource("HelloWorld.java");
        CompilationUnit helloWorld2 = parseResource("HelloWorld.java");

        assertEquals(helloWorld, helloWorld2);
    }

    @Test
    public void differingAstsAreNotEqual() throws IOException {
        CompilationUnit helloWorld = parseResource("HelloWorld.java");
        CompilationUnit helloWord = parseResource("HelloWorld_HelloWord.java");

        assertNotEquals(helloWorld, helloWord);
    }

    @Test
    public void astEqualityIgnoresWhitespace() throws IOException {
        CompilationUnit helloWorld = parseResource("HelloWorld.java");
        CompilationUnit helloWorldOneLine = parseResource("HelloWorld_Oneliner.java");

        assertEquals(helloWorld, helloWorldOneLine);
    }

    @Test
    public void astEqualityDoesNotIgnoreComments() throws IOException {
        CompilationUnit helloWorld = parseResource("HelloWorld.java");
        CompilationUnit helloWorldCommented = parseResource("HelloWorld_Comment.java");

        assertNotEquals(helloWorld, helloWorldCommented);
    }
}
