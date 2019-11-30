package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JmhFixtureImproverTest {
    private static final AstResourceLoader astLoader =
            new AstResourceLoader(JmhFixtureImproverTest.class);


    @ParameterizedTest
    @ValueSource(strings = {
            "IncludeFixtureMethods",
            "ReturnValue",
            "NoTearDown",
            "Annotations"
    })
    public void producesExpectedOutput(String testCase) throws IOException {
        CompilationUnit ast = astLoader.load(testCase + "_Input.java");
        CompilationUnit expected = astLoader.load(testCase + "_Expected.java");

        JmhFixtureImprover.improveFixture(ast);

        assertEquals(expected, StaticJavaParser.parse(ast.toString()));
    }
}
