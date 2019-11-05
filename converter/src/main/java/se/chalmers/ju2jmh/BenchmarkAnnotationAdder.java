package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.openjdk.jmh.annotations.Benchmark;

public class BenchmarkAnnotationAdder {
    private static final String TEST_ANNOTATION_NAME = "Test";
    private static final String BENCHMARK_ANNOTATION_QUALIFIED_NAME =
            Benchmark.class.getCanonicalName();

    private BenchmarkAnnotationAdder() {
        throw new AssertionError("Should not be instantiated.");
    }

    public static void addBenchmarkAnnotations(CompilationUnit compilationUnit) {
        for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
            type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(TEST_ANNOTATION_NAME))
                    .forEach(m -> m.addAnnotation(BENCHMARK_ANNOTATION_QUALIFIED_NAME));
        }
    }
}
