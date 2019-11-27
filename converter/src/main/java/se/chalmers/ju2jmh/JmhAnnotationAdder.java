package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import org.openjdk.jmh.annotations.*;

public class JmhAnnotationAdder {
    private static final String TEST = "Test";
    private static final String IGNORE = "Ignore";
    private static final String BEFORE = "Before";
    private static final String AFTER = "After";

    private static final AnnotationExpr BENCHMARK_ANNOTATION = jmhBenchmarkAnnotation();
    private static final AnnotationExpr STATE_ANNOTATION = jmhStateAnnotation();
    private static final AnnotationExpr SET_UP_ANNOTATION = jmhSetUpAnnotation();
    private static final AnnotationExpr TEAR_DOWN_ANNOTATION = jmhTearDownAnnotation();

    private JmhAnnotationAdder() {
        throw new AssertionError("Should not be instantiated.");
    }

    private static AnnotationExpr jmhBenchmarkAnnotation() {
        return new MarkerAnnotationExpr().setName(Benchmark.class.getCanonicalName());
    }

    private static AnnotationExpr jmhStateAnnotation() {
        return new SingleMemberAnnotationExpr()
                .setMemberValue(new FieldAccessExpr()
                        .setScope(new TypeExpr().setType(Scope.class.getCanonicalName()))
                        .setName(Scope.Thread.name()))
                .setName(State.class.getCanonicalName());
    }

    private static AnnotationExpr jmhFixtureAnnotation() {
        return new SingleMemberAnnotationExpr()
                .setMemberValue(new FieldAccessExpr()
                        .setScope(new TypeExpr().setType(Level.class.getCanonicalName()))
                        .setName(Level.Invocation.name()));
    }

    private static AnnotationExpr jmhSetUpAnnotation() {
        return jmhFixtureAnnotation().setName(Setup.class.getCanonicalName());
    }

    private static AnnotationExpr jmhTearDownAnnotation() {
        return jmhFixtureAnnotation().setName(TearDown.class.getCanonicalName());
    }

    private static boolean hasTests(TypeDeclaration<?> type) {
        return type.getMethods().stream()
                .anyMatch(m -> m.isAnnotationPresent(TEST));
    }

    private static boolean hasFixtureMethods(TypeDeclaration<?> type) {
        return type.getMethods().stream()
                .anyMatch(m -> m.isAnnotationPresent(BEFORE)
                        || m.isAnnotationPresent(AFTER));
    }

    public static void addBenchmarkAnnotations(CompilationUnit compilationUnit) {
        for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
            if (!hasTests(type) && !hasFixtureMethods(type)) {
                continue;
            }
            type.addAnnotation(STATE_ANNOTATION);
            type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(TEST))
                    .filter(m -> !m.isAnnotationPresent(IGNORE))
                    .forEach(m -> m.addAnnotation(BENCHMARK_ANNOTATION));
            type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(BEFORE))
                    .forEach(m -> m.addAnnotation(SET_UP_ANNOTATION));
            type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(AFTER))
                    .forEach(m -> m.addAnnotation(TEAR_DOWN_ANNOTATION));
        }
    }
}
