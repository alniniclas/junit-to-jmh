package se.chalmers.ju2jmh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.testinput.unittests.junit5.DisabledTest;
import se.chalmers.ju2jmh.testinput.unittests.junit5.StaticLifecycleTest;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConverterCombinationTest {

    @Test
    public void convertsClassAndSpecificMethodTogether(@TempDir Path tempDir) throws Exception {
        Converter converter = new Converter();
        CommandLine cmd = new CommandLine(converter);

        InputClassDirectory dir = new InputClassDirectory(tempDir);
        dir.add(StaticLifecycleTest.class);
        dir.add(DisabledTest.class);

        String src = dir.sourcesDirectory().toString();
        String classes = dir.bytecodeDirectory().toString();
        Path outPath = tempDir.resolve("out");
        Files.createDirectories(outPath);
        String out = outPath.toString();

        String classWhole = StaticLifecycleTest.class.getName();
        String classMethod = DisabledTest.class.getName() + "#activeTest";

        String[] args = new String[]{src, classes, out, classWhole, classMethod};
        cmd.parseArgs(args);

        // run converter
        converter.call();

        // Check outputs
        File staticFile = outPath.resolve(StaticLifecycleTest.class.getName().replace('.', File.separatorChar) + ".java").toFile();
        File disabledFile = outPath.resolve(DisabledTest.class.getName().replace('.', File.separatorChar) + ".java").toFile();

        assertTrue(staticFile.exists(), "StaticLifecycleTest output should exist");
        assertTrue(disabledFile.exists(), "DisabledTest output should exist");

        String staticContent = Files.readString(staticFile.toPath());
        String disabledContent = Files.readString(disabledFile.toPath());

        // Expect benchmark methods generated
        assertTrue(disabledContent.contains("benchmark_activeTest"), "DisabledTest should contain benchmark_activeTest");
        assertTrue(staticContent.contains("benchmark_benchmarkWorkload"), "StaticLifecycleTest should contain benchmark_benchmarkWorkload");
    }
}

