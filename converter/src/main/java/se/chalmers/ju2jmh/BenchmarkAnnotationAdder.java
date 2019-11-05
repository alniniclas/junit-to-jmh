package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.openjdk.jmh.annotations.Benchmark;

public class BenchmarkAnnotationAdder {
    private static final String BENCHMARK_ANNOTATION_QUALIFIED_NAME =
            Benchmark.class.getCanonicalName();

    private BenchmarkAnnotationAdder() {
        throw new AssertionError("Should not be instantiated.");
    }

    public static CompilationUnit addBenchmarkAnnotations(CompilationUnit compilationUnit) {
        CompilationUnit extracted = compilationUnit.clone();
        for (TypeDeclaration<?> type : extracted.getTypes()) {
            type.getMethods().forEach(m -> m.addAnnotation(BENCHMARK_ANNOTATION_QUALIFIED_NAME));
        }
        return extracted;
    }
}
