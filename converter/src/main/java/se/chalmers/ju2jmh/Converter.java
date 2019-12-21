package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "ju2jmh", mixinStandardHelpOptions = true)
public class Converter implements Callable<Integer> {
    @CommandLine.Parameters(description =
            "Root path(s) of the input source files. `${sys:path.separator}` may be used as a"
                    + " separator to specify multiple directories.", index = "0")
    private String sourcePath;

    @CommandLine.Parameters(description =
            "Root path(s) of the input class files. `${sys:path.separator}` may be used as a"
                    + " separator to specify multiple directories.", index = "1")
    private String classPath;

    @CommandLine.Parameters(description = "Root path for the output source files.", index = "2")
    private Path outputPath;

    @CommandLine.Parameters(
            description = "Fully qualified names of the classes to convert to benchmarks.",
            index = "3..*")
    private List<String> classNames;

    @CommandLine.Option(
            names = {"--ju4-runner-benchmark"},
            description = "Generate benchmarks delegating their execution to the JUnit 4 JUnitCore "
                    + "runner.")
    private boolean ju4RunnerBenchmark;

    @CommandLine.Option(
            names = {"-i", "--ignore-failures"},
            description = "Generate the remaining benchmark classes even if conversion of some "
                    + "input classes fails.")
    private boolean ignoreFailures;

    @CommandLine.Option(
            names = {"--class-names-file"},
            description = "File to load class names from.")
    private Path classNamesFile;

    private void writeSourceCodeToFile(CompilationUnit benchmark, File outputFile)
            throws IOException {
        outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();
        try (OutputStreamWriter out = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            out.append(benchmark.toString());
        }
    }

    private void generateNestedBenchmarks() throws ClassNotFoundException, IOException {
        NestedBenchmarkSuiteBuilder benchmarkSuiteBuilder =
                new NestedBenchmarkSuiteBuilder(toPaths(sourcePath), toPaths(classPath));
        for (String className : classNames) {
            benchmarkSuiteBuilder.addTestClass(className);
        }
        Map<String, CompilationUnit> suite = benchmarkSuiteBuilder.buildSuite();
        for (String className : suite.keySet()) {
            File outputFile =
                    outputPath.resolve(className.replace('.', File.separatorChar) + ".java")
                            .toFile();
            writeSourceCodeToFile(suite.get(className), outputFile);
        }
    }

    private void generateJU4Benchmarks()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        InputClassRepository repository =
                new InputClassRepository(toPaths(sourcePath), toPaths(classPath));
        JU4BenchmarkFactory benchmarkFactory = new JU4BenchmarkFactory(repository);
        List<CompilationUnit> benchmarks = new ArrayList<>(classNames.size());
        for (String className : classNames) {
            try {
                benchmarks.add(benchmarkFactory.createBenchmarkFromTest(className));
            } catch (BenchmarkGenerationException e) {
                if (!ignoreFailures) {
                    throw e;
                }
            }
        }
        for (CompilationUnit benchmark : benchmarks) {
            TypeDeclaration<?> benchmarkClass = benchmark.getTypes().get(0);
            String benchmarkClassName = benchmarkClass.getFullyQualifiedName().orElseThrow();
            File outputFile = outputPath.resolve(
                    benchmarkClassName.replace('.', File.separatorChar) + ".java").toFile();
            writeSourceCodeToFile(benchmark, outputFile);
        }
    }

    private static List<Path> toPaths(String pathString) {
        return Arrays.stream(pathString.split(File.pathSeparator))
                .map(p -> Path.of(p))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Integer call() throws ClassNotFoundException, IOException, InvalidInputClassException {
        if (!outputPath.toFile().exists()) {
            throw new FileNotFoundException("Output directory " + outputPath + " does not exist");
        }
        if (classNamesFile != null) {
            try (Stream<String> lines = Files.lines(classNamesFile)) {
                if (classNames == null) {
                    classNames = lines.collect(Collectors.toUnmodifiableList());
                } else {
                    classNames = Stream.concat(classNames.stream(), lines).collect(Collectors.toUnmodifiableList());
                }
            }
        }
        if (!ju4RunnerBenchmark) {
            generateNestedBenchmarks();
        } else {
            generateJU4Benchmarks();
        }
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Converter()).execute(args);
        System.exit(exitCode);
    }
}
