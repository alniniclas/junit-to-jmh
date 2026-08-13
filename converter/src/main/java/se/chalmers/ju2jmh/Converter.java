package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import picocli.CommandLine;
import se.chalmers.ju2jmh.api.ExceptionTest;
import se.chalmers.ju2jmh.api.JU2JmhBenchmark;
import se.chalmers.ju2jmh.api.Rules;
import se.chalmers.ju2jmh.api.ThrowingConsumer;
import se.chalmers.ju2jmh.model.UnitTestClass;
import com.github.javaparser.ParserConfiguration.LanguageLevel; //language level
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
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


    /* JUNIT 5 AND JUNIT4 BENCHMARKS GENERATION OPTIONS */
    @CommandLine.Option(
            names = {"--ju-runner-benchmark"},
            description = "Generate benchmarks delegating their execution to the underlying "
                    + "JUnit runner (supports both JUnit 4 and JUnit 5).")
    private boolean juRunnerBenchmark;

    @CommandLine.Option(
            names = {"--tailored-benchmark"},
            description = "Generate benchmarks customised to and optimised based on the specific "
                    + "JUnit features used by the individual tests.")
    private boolean tailoredBenchmark;

    @CommandLine.Option(
            names = {"-i", "--ignore-failures"},
            description = "Generate the remaining benchmark classes even if conversion of some "
                    + "input classes fails.")
    private boolean ignoreFailures;

    @CommandLine.Option(
            names = {"--class-names-file"},
            description = "File to load class names from.")
    private Path classNamesFile;

    @CommandLine.Option(
            names = {"-m", "--methods"},
            description = "Specific methods to convert (comma separated). If omitted, all tests are converted.",
            split = ","
    )

    private List<String> targetMethods;


    @CommandLine.Option(
            names = {"--on-conflict"},
            description = "Policy when an output file already exists: ask|overwrite|merge. Default: ask"
    )
    private String onConflict = "ask";

    @CommandLine.Option(names = {"-q", "--quiet"}, description = "Suppress output logs like [CREATE], [OVERWRITE], etc.")
    private boolean quiet = false;

    private static CompilationUnit loadApiSource(Class<?> apiClass) throws IOException {
        return StaticJavaParser.parseResource(
                apiClass.getCanonicalName().replace('.', '/') + ".java");
    }


    private void writeSourceCodeToFile(CompilationUnit benchmark, File outputFile) throws IOException {
        outputFile.getParentFile().mkdirs();

        // 1 if file NOT exists create it
        if (!outputFile.exists()) {
            if (!quiet) System.out.println("[CREATE] " + outputFile.getPath());

            //write file
            try (OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                out.append(benchmark.toString());
            }
            return;
        }

        //   Jump if content is the SAME
        try {
            String existingContent = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8);
            //  delete string spaces
            if (existingContent.trim().equals(benchmark.toString().trim())) {
                return;
            }
        } catch (IOException e) {
        }
        // 3  if file EXISTS and content is DIFFERENT
        String policy = (onConflict == null) ? "ask" : onConflict.toLowerCase();
        boolean doOverwrite = false;
        boolean doMerge = false;

        if (policy.equals("overwrite")) doOverwrite = true;
        else if (policy.equals("merge")) doMerge = true;

        else { // ask
            String prompt = String.format("File %s has CHANGED. Choose: [o]verwrite, [m]erge (keep previous benchmarks), [s]kip: ", outputFile.getName());
            String line = null;

            Console console = System.console();
            if (console != null) {
                //  terminal
                line = console.readLine(prompt);
            } else { //ide
                System.out.print(prompt);
                System.out.flush();
                try {
                    java.util.Scanner scanner = new java.util.Scanner(System.in);
                    if (scanner.hasNextLine()) {
                        line = scanner.nextLine();
                    }
                } catch (Exception e) {
                }
            }

            if (line != null && line.trim().toLowerCase().startsWith("m")) doMerge = true;
            else if (line != null && line.trim().toLowerCase().startsWith("o")) doOverwrite = true;
            else if (line != null && line.trim().toLowerCase().startsWith("s")) return;
            else {

                doOverwrite = true;
            }
        }

        if (doOverwrite) {
            if (!quiet) System.out.println("[OVERWRITE] " + outputFile.getPath());
            createBackup(outputFile);
            try (OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(outputFile, false), StandardCharsets.UTF_8)) {
                out.append(benchmark.toString());
            }
            return;
        }

        if (doMerge) {
            try {
                CompilationUnit existingCU = StaticJavaParser.parse(outputFile);
                // Merge IMPORTS
                existingCU.getImports().forEach(im -> {
                    if (!benchmark.getImports().contains(im)) benchmark.addImport(im);
                });

                //check if the main type (class/interface) is the same, otherwise we cannot merge
                TypeDeclaration<?> newType = benchmark.getTypes().get(0);
                TypeDeclaration<?> existingType = existingCU.getTypes().stream()
                        .filter(t -> t.getNameAsString().equals(newType.getNameAsString()))
                        .findFirst().orElse(null);



                // control that both types are class or interface declarations, otherwise we cannot merge
                if (existingType != null && existingType.isClassOrInterfaceDeclaration() && newType.isClassOrInterfaceDeclaration()) {

                    //convert to ClassOrInterfaceDeclaration
                    ClassOrInterfaceDeclaration existingCid = existingType.asClassOrInterfaceDeclaration();
                    ClassOrInterfaceDeclaration newCid = newType.asClassOrInterfaceDeclaration();

                    // merge OUTER class methods
                    mergeMethods(existingCid, newCid);

                    // find inner class named _Benchmark
                    existingCid.getMembers().stream()
                            .filter(member -> member.isClassOrInterfaceDeclaration())
                            .map(member -> member.asClassOrInterfaceDeclaration())
                            .filter(inner -> inner.getNameAsString().equals("_Benchmark"))
                            .findFirst()
                            .ifPresent(existingInner -> {
                                newCid.getMembers().stream()
                                        .filter(m -> m.isClassOrInterfaceDeclaration())
                                        .map(m -> m.asClassOrInterfaceDeclaration())
                                        .filter(newInner -> newInner.getNameAsString().equals("_Benchmark"))
                                        .findFirst()
                                        .ifPresent(newInner -> {
                                            //MERGE INNER class methods _BENCHMARK
                                            mergeMethods(existingInner, newInner);
                                        });
                            });

                    if (!quiet) System.out.println("[MERGE] " + outputFile.getPath());
                    createBackup(outputFile);

                    try (OutputStreamWriter out = new OutputStreamWriter(
                            new FileOutputStream(outputFile, false), StandardCharsets.UTF_8)) {
                        out.append(benchmark.toString());
                    }
                }
            }  catch (Exception e) {
            System.err.println("[WARN] Merge failed for " + outputFile.getName() + ": " + e.getMessage() + ". Overwriting.");
            createBackup(outputFile);
            try (OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(outputFile, false), StandardCharsets.UTF_8)) {
                out.append(benchmark.toString());
            }
        }
        }
    }


    private void mergeMethods(ClassOrInterfaceDeclaration source, ClassOrInterfaceDeclaration dest) {
        List<MethodDeclaration> oldBenchmarks = source.getMethods().stream()
                .filter(m -> m.getNameAsString().startsWith("benchmark_"))
                .collect(Collectors.toList());

        int count = 0;
        for (MethodDeclaration oldMethod : oldBenchmarks) {
            boolean present = dest.getMethods().stream()
                    .anyMatch(nm -> nm.getNameAsString().equals(oldMethod.getNameAsString()));
            if (!present) {
                dest.addMember(oldMethod.clone());
                count++;
            }
        }
        if (count > 0) {
            System.out.println("   -> Recovered " + count + " methods from " + source.getNameAsString());
        }
    }

    private void generateNestedBenchmarks() throws ClassNotFoundException, IOException, InvalidInputClassException {
        InputClassRepository repository = new InputClassRepository(toPaths(sourcePath), toPaths(classPath));

        //builder and add all classes/methods to it
        NestedBenchmarkSuiteBuilder benchmarkSuiteBuilder =
                new NestedBenchmarkSuiteBuilder(toPaths(sourcePath), toPaths(classPath));

        // keep track of classes with Map
        Map<String, List<String>> explicitlyRequestedMethods = new HashMap<>();

        for (String inputArg : classNames) {
            String className = inputArg;
            List<String> methodsForThisClass = new ArrayList<>();

            //  # method1,method2
            if (inputArg.contains("#")) {
                String[] parts = inputArg.split("#", 2);
                className = parts[0];
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    methodsForThisClass.addAll(Arrays.asList(parts[1].split(",")));
                    explicitlyRequestedMethods.put(className, methodsForThisClass);
                }
            } else {
                // -m
                if (targetMethods != null && !targetMethods.isEmpty()) {
                    methodsForThisClass.addAll(targetMethods);
                }
            }

            // Print if not in quiet mode
            if (!quiet) {
                if (methodsForThisClass.isEmpty()) {
                    System.out.println("Converting ENTIRE class: " + className);
                } else {
                    System.out.println("PARTIALLY converting " + className + " methods: " + methodsForThisClass);
                }
            }

            // Add the class to the builder with the specified list
            if (methodsForThisClass.isEmpty()) {
                benchmarkSuiteBuilder.addTestClass(className);
            } else {
                benchmarkSuiteBuilder.addTestClass(className, methodsForThisClass);
            }
            }

        Map<String, CompilationUnit> suite = benchmarkSuiteBuilder.buildSuite();


        if (!explicitlyRequestedMethods.isEmpty()) {
            for (Map.Entry<String, List<String>> e : explicitlyRequestedMethods.entrySet()) {
                String className = e.getKey();
                boolean foundBenchmarksForClass = false;
                CompilationUnit generated = suite.get(className);

                if (generated != null) {
                    for (TypeDeclaration<?> type : generated.getTypes()) {
                        List<MethodDeclaration> methods = type.findAll(MethodDeclaration.class);
                        for (MethodDeclaration md : methods) {
                            if (md.getNameAsString().startsWith("benchmark_")) {
                                foundBenchmarksForClass = true;
                                break;
                            }
                        }
                        if (foundBenchmarksForClass) break;
                    }
                }
                if (!foundBenchmarksForClass) {
                    String msg = "Requested methods " + e.getValue() + " not found for class " + className;
                    if (!ignoreFailures) {
                        throw new InvalidInputClassException(msg);
                    } else {
                        System.err.println("Warning: " + msg);
                    }
                }
            }
        }

        for (String generatedClassName : suite.keySet()) {
            File outputFile = outputPath.resolve(
                    generatedClassName.replace('.', File.separatorChar) + ".java").toFile();
            writeSourceCodeToFile(suite.get(generatedClassName), outputFile);
        }

        //  write JU2JmhBenchmark API class
        writeSourceCodeToFile(
                loadApiSource(JU2JmhBenchmark.class),
                outputPath.resolve(
                                JU2JmhBenchmark.class.getCanonicalName()
                                        .replace('.', File.separatorChar) + ".java")
                        .toFile());
    }



    private void generateJUBenchmarks()
            throws ClassNotFoundException, IOException, InvalidInputClassException {
        InputClassRepository repository =
                new InputClassRepository(toPaths(sourcePath), toPaths(classPath));
        WrapperBenchmarkFactory benchmarkFactory = new WrapperBenchmarkFactory(repository);
        List<CompilationUnit> benchmarks = new ArrayList<>(classNames.size());

        for (String className : classNames) {

            if (className.contains("#")) {
                String[] parts = className.split("#");
                String baseClassName = parts[0];
                String[] requestedMethods = parts[1].split(",");

                // get source of the class
                TypeDeclaration<?> testClassSource = repository.findClass(baseClassName).getSource();

                // extract method names from the source
                Set<String> actualMethodsInSource = testClassSource.findAll(MethodDeclaration.class)
                        .stream()
                        .map(MethodDeclaration::getNameAsString)
                        .collect(Collectors.toSet());

                // Validate that all requested methods are present in the source
                for (String requested : requestedMethods) {
                    String trimmedRequested = requested.trim();
                    if (!actualMethodsInSource.contains(trimmedRequested)) {
                        throw new InvalidInputClassException(
                                "\n[ERRORE] Metodo '" + trimmedRequested +
                                        "' non trovato nella classe " + baseClassName);
                    }
                }
            }

            try {
                benchmarks.add(benchmarkFactory.createBenchmarkFromTest(className));
            } catch (BenchmarkGenerationException e) {
                if (!ignoreFailures) {
                    throw e;
                }
                System.err.println("[WARNING] Skipping " + className + " due to conversion error: " + e.getMessage());
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


    private static void loadMissingCompilationUnits(
            String packageName, File file, InputClassRepository repository,
            Map<String, CompilationUnit> compilationUnits) throws ClassNotFoundException {
        String name = file.getName();
        if (file.isFile()) {
            if (name.endsWith(".java")) {
                String className =
                        packageName + name.substring(0, name.length() - ".java".length());
                if (!compilationUnits.containsKey(className)) {
                    compilationUnits.put(
                            className,
                            repository.findClass(className)
                                    .getSource()
                                    .findCompilationUnit()
                                    .orElseThrow());
                }
            }
        } else if (file.isDirectory()) {
            packageName = packageName + name + ".";
            File[] containedFiles = file.listFiles();
            if (containedFiles == null) {
                return;
            }
            for (File containedFile : containedFiles) {
                loadMissingCompilationUnits(
                        packageName, containedFile, repository, compilationUnits);
            }
        }
    }

    private void generateTailoredBenchmarks() throws ClassNotFoundException, IOException {
        InputClassRepository repository =
                new InputClassRepository(toPaths(sourcePath), toPaths(classPath));
        UnitTestClassRepository testClassRepository = new UnitTestClassRepository(repository);
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        for (String className : classNames) {
            UnitTestClass testClass = testClassRepository.findClass(className);
            Predicate<String> nameValidator =
                    TailoredBenchmarkFactory.nameValidatorForCompilationUnit(
                            repository.findClass(className)
                                    .getSource()
                                    .findCompilationUnit()
                                    .orElseThrow());
            ClassOrInterfaceDeclaration benchmarkClass =
                    TailoredBenchmarkFactory.generateBenchmarkClass(testClass, nameValidator);
            TypeDeclaration<?> testClassSource = repository.findClass(className).getSource();
            testClassSource.addMember(benchmarkClass);
            compilationUnits.put(className, testClassSource.findCompilationUnit().orElseThrow());
        }
        for (Path path : toPaths(sourcePath)) {
            File[] files = path.toFile().listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                loadMissingCompilationUnits("", file, repository, compilationUnits);
            }
        }
        compilationUnits.put(
                ExceptionTest.class.getCanonicalName(), loadApiSource(ExceptionTest.class));
        compilationUnits.put(Rules.class.getCanonicalName(), loadApiSource(Rules.class));
        compilationUnits.put(
                ThrowingConsumer.class.getCanonicalName(), loadApiSource(ThrowingConsumer.class));
        for (String className : compilationUnits.keySet()) {
            CompilationUnit benchmark = compilationUnits.get(className);
            File outputFile = outputPath.resolve(
                    className.replace('.', File.separatorChar) + ".java").toFile();
            writeSourceCodeToFile(benchmark, outputFile);
        }
    }

    private static List<Path> toPaths(String pathString) {
        return Arrays.stream(pathString.split(File.pathSeparator))
                .map(Path::of)
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
        if (!juRunnerBenchmark) {
            if (!tailoredBenchmark) {
                generateNestedBenchmarks();
            } else {
                generateTailoredBenchmarks();
            }
        } else {
            generateJUBenchmarks();
        }
        return 0;
    }


    //  HELPER BACKUP
    private void createBackup(File file) {
        try {
            Path backupPath = Path.of(file.getAbsolutePath() + ".bak");
            Files.copy(file.toPath(), backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("[WARN] Failed to create backup for " + file.getName() + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        StaticJavaParser.getConfiguration().setLanguageLevel(LanguageLevel.JAVA_17);
        int exitCode = new CommandLine(new Converter()).execute(args);
        System.exit(exitCode);
    }
}
