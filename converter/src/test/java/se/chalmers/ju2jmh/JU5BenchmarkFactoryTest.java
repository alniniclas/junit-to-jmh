package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.testinput.unittests.SimpleJUnit5Test; 
import java.io.IOException;
import java.nio.file.Path;
import static org.hamcrest.MatcherAssert.assertThat;
import static se.chalmers.ju2jmh.AstMatcher.equalsAst;

public class JU5BenchmarkFactoryTest {

    private static InputClassRepository repository;

    private static final String EXPECTED_CODE = " PLACEHOLDER "; 

    @BeforeAll
    public static void setUpRepository(@TempDir Path tempDir) throws IOException, ClassNotFoundException {
        InputClassDirectory inputClassDirectory = InputClassDirectory.directoryWithClasses(
                tempDir,
                SimpleJUnit5Test.class
        );
        repository = new InputClassRepository(inputClassDirectory.sourcesDirectory(), inputClassDirectory.bytecodeDirectory());
    }

    @Test
    public void producesLauncherBenchmarkFromSimpleJUnit5Test() throws ClassNotFoundException, IOException, InvalidInputClassException {
        WrapperBenchmarkFactory benchmarkFactory = new WrapperBenchmarkFactory(repository);

        CompilationUnit generated = benchmarkFactory.createBenchmarkFromTest(SimpleJUnit5Test.class.getName());

    }
}
