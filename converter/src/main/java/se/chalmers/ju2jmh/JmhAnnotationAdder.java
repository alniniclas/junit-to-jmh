package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import org.openjdk.jmh.annotations.*;

public class JmhAnnotationAdder {
    private static final String TEST_ANNOTATION_NAME = "Test";
    private static final String BEFORE_ANNOTATION_NAME = "Before";
    private static final String AFTER_ANNOTATION_NAME = "After";


    private JmhAnnotationAdder() {
        throw new AssertionError("Should not be instantiated.");
    }

    private static boolean hasTests(TypeDeclaration<?> type) {
        return type.getMethods().stream()
                .anyMatch(m -> m.isAnnotationPresent(TEST_ANNOTATION_NAME));
    }

    private static boolean hasFixtureMethods(TypeDeclaration<?> type) {
        return type.getMethods().stream()
                .anyMatch(m -> m.isAnnotationPresent(BEFORE_ANNOTATION_NAME)
                        || m.isAnnotationPresent(AFTER_ANNOTATION_NAME));
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

    public static void addBenchmarkAnnotations(CompilationUnit compilationUnit) {
        for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
            if (!hasTests(type) && !hasFixtureMethods(type)) {
                continue;
            }
            AnnotationExpr benchmarkAnnotation = jmhBenchmarkAnnotation();
            AnnotationExpr stateAnnotation = jmhStateAnnotation();
            AnnotationExpr setUpAnnotation = jmhSetUpAnnotation();
            AnnotationExpr tearDownAnnotation = jmhTearDownAnnotation();
            type.addAnnotation(stateAnnotation);
            type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(TEST_ANNOTATION_NAME))
                    .forEach(m -> m.addAnnotation(benchmarkAnnotation));
            type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(BEFORE_ANNOTATION_NAME))
                    .forEach(m -> m.addAnnotation(setUpAnnotation));
            type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(AFTER_ANNOTATION_NAME))
                    .forEach(m -> m.addAnnotation(tearDownAnnotation));
        }
    }
}
