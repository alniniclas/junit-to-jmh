package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.google.common.io.Resources;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.Method;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JU4BenchmarkFactory {
    private static final String CLASS_TEMPLATE_NAME = "templates/ju4_benchmark/class_template.java";
    private static final String METHOD_TEMPLATE_NAME =
            "templates/ju4_benchmark/method_template.java";
    private static final String J_UNIT_4_TEST_ANNOTATION = "Lorg/junit/Test;";
    private static final String TEST_CLASS_PLACEHOLDER = "TEST_CLASS";
    private static final String TEST_METHOD_NAME_PLACEHOLDER = "TEST_METHOD_NAME";
    private final SourceClassRepository repository;

    public static class BenchmarkTemplateException extends RuntimeException {
        public BenchmarkTemplateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public JU4BenchmarkFactory(SourceClassRepository repository) {
        this.repository = repository;
    }

    private static String loadTemplate(String templateName) {
        @SuppressWarnings("UnstableApiUsage")
        URL resourceUrl = Resources.getResource(templateName);
        try {
            @SuppressWarnings("UnstableApiUsage")
            String template = Resources.toString(resourceUrl, StandardCharsets.UTF_8);
            return template;
        } catch (IOException e) {
            throw new BenchmarkTemplateException("Failed to load benchmark template", e);
        }
    }

    private static CompilationUnit loadClassTemplate() {
        return StaticJavaParser.parse(loadTemplate(CLASS_TEMPLATE_NAME));
    }

    private static MethodDeclaration loadMethodTemplate() {
        return StaticJavaParser.parseMethodDeclaration(loadTemplate(METHOD_TEMPLATE_NAME));
    }

    private static Predicate<Method> isJUnit4Test() {
        return m -> Arrays.stream(m.getAnnotationEntries())
                .anyMatch(a -> a.getAnnotationType().equals(J_UNIT_4_TEST_ANNOTATION));
    }

    private Stream<String> findTestMethods(SourceClass sourceClass) {
        Stream<String> declaredTestMethods = Arrays.stream(sourceClass.getBytecode().getMethods())
                .filter(isJUnit4Test())
                .map(FieldOrMethod::getName);
        SourceClass superclass = null;
        try {
            superclass = repository.findClass(sourceClass.getSuperclassName());
            return Stream.concat(findTestMethods(superclass), declaredTestMethods).distinct();
        } catch (ClassNotFoundException e) {
            return declaredTestMethods.distinct();
        }
    }

    private static ModifierVisitor<Void> expressionReplacementVisitor(
            String placeholder, Visitable replacement) {
        return new ModifierVisitor<>() {
            @Override
            public Visitable visit(NameExpr n, Void arg) {
                if (n.getNameAsString().equals(placeholder)) {
                    return replacement;
                }
                return n;
            }
        };
    }

    private CompilationUnit generateBenchmark(
            String packageName, String benchmarkClassName, String testClassName,
            List<String> testMethodNames) {
        CompilationUnit output = loadClassTemplate();
        if (packageName != null) {
            output.setPackageDeclaration(packageName);
        }
        TypeDeclaration<?> outputClass = output.getType(0);
        outputClass.setName(benchmarkClassName);
        Expression testClassExpression = StaticJavaParser.parseExpression(
                testClassName + ".class");
        outputClass.accept(
                expressionReplacementVisitor(TEST_CLASS_PLACEHOLDER, testClassExpression), null);
        MethodDeclaration methodTemplate = loadMethodTemplate();
        for (String testMethodName : testMethodNames) {
            MethodDeclaration benchmarkMethod = methodTemplate.clone();
            benchmarkMethod.setName("benchmark_" + testMethodName);
            benchmarkMethod.accept(expressionReplacementVisitor(
                    TEST_METHOD_NAME_PLACEHOLDER, new StringLiteralExpr(testMethodName)),null);
            outputClass.addMember(benchmarkMethod);
        }
        return output;
    }

    public CompilationUnit createBenchmarkFromTest(String testClassName)
            throws ClassNotFoundException {
        SourceClass sourceClass = repository.findClass(testClassName);
        TypeDeclaration<?> source = sourceClass.getSource();
        String testClassCanonicalNameWithoutPackage =
                testClassName.substring(testClassName.lastIndexOf('.') + 1)
                        .replace('$', '.');
        String benchmarkClassName =
                testClassCanonicalNameWithoutPackage.replace('.', '_') + "_JU4Benchmark";
        List<String> testMethodNames = findTestMethods(sourceClass)
                .collect(Collectors.toUnmodifiableList());
        String packageName = source.findCompilationUnit()
                .orElseThrow()
                .getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse(null);
        return generateBenchmark(
                packageName, benchmarkClassName,
                testClassCanonicalNameWithoutPackage, testMethodNames);
    }
}
