package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.apache.bcel.classfile.JavaClass;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
            description = "fully qualified names of the classes to convert to benchmarks.",
            index = "3..*")
    private List<String> classNames;

    @CommandLine.Option(
            names = {"-f", "--improve-fixture"},
            description = "Improve the generated benchmarks by calling invocation-level fixture "
                    + "methods directly from the benchmark methods themselves.")
    private boolean improveFixture;

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

    private void writeBenchmarkToFile(CompilationUnit benchmark, File outputFile)
            throws IOException {
        outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();
        try (OutputStreamWriter out = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            out.append(benchmark.toString());
        }
    }

    private void generateRegularBenchmarks(InputClassRepository repository)
            throws ClassNotFoundException, IOException {
        for (String className : classNames) {
            InputClass inputClass = repository.findClass(className);
            CompilationUnit ast = inputClass.getSource().findCompilationUnit().orElseThrow();
            ExceptionTestRestructurer.restructureExceptionTests(ast);
            JmhAnnotationAdder.addBenchmarkAnnotations(ast);
            if (improveFixture) {
                JmhFixtureImprover.improveFixture(ast);
            }
            JavaClass bytecode = inputClass.getBytecode();
            File outputFile =
                    outputPath.resolve(bytecode.getPackageName().replace('.', File.separatorChar))
                            .resolve(bytecode.getSourceFileName())
                            .toFile();
            writeBenchmarkToFile(ast, outputFile);
        }
    }

    private void generateJU4Benchmarks(InputClassRepository repository)
            throws ClassNotFoundException, IOException, InvalidInputClassException {
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
            writeBenchmarkToFile(benchmark, outputFile);
        }
    }

    private static List<Path> toPaths(String pathString) {
        return Arrays.stream(pathString.split(File.pathSeparator))
                .map(p -> Path.of(p))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Integer call() throws ClassNotFoundException, IOException, InvalidInputClassException {
        InputClassRepository repository =
                new InputClassRepository(toPaths(sourcePath), toPaths(classPath));
        if (!outputPath.toFile().exists()) {
            throw new FileNotFoundException("Output directory " + outputPath + " does not exist");
        }
        if (!ju4RunnerBenchmark) {
            generateRegularBenchmarks(repository);
        } else {
            generateJU4Benchmarks(repository);
        }
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Converter()).execute(args);
        System.exit(exitCode);
    }
}
