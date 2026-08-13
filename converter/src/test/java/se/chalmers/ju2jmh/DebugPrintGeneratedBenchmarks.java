package se.chalmers.ju2jmh;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class DebugPrintGeneratedBenchmarks {
    @Test
    public void printGenerated() throws Exception {
        Path tempDir = java.nio.file.Files.createTempDirectory("debugrepo");
        InputClassDirectory inputClassDirectory = InputClassDirectory.directoryWithClasses(
                tempDir, se.chalmers.ju2jmh.testinput.unittests.TestImplementation.class,
                se.chalmers.ju2jmh.testinput.unittests.ClassWithNestedTests.Nested.class
        );
        InputClassRepository repository = new InputClassRepository(inputClassDirectory.sourcesDirectory(), inputClassDirectory.bytecodeDirectory());
        WrapperBenchmarkFactory factory = new WrapperBenchmarkFactory(repository);

        System.out.println("---- TestImplementation generated ----");
        var cu1 = factory.createBenchmarkFromTest(se.chalmers.ju2jmh.testinput.unittests.TestImplementation.class.getName());
        System.out.println(cu1.toString());

        System.out.println("---- ClassWithNestedTests.Nested generated ----");
        var cu2 = factory.createBenchmarkFromTest(se.chalmers.ju2jmh.testinput.unittests.ClassWithNestedTests.Nested.class.getName());
        System.out.println(cu2.toString());
    }
}
