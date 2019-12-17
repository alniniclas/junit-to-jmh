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
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JU4BenchmarkFactory {
    private static final CompilationUnit CLASS_TEMPLATE =
            AstTemplates.compilationUnit("templates/ju4_benchmark/class_template.java");
    private static final MethodDeclaration METHOD_TEMPLATE =
            AstTemplates.method("templates/ju4_benchmark/method_template.java");
    private static final String J_UNIT_4_TEST_ANNOTATION = "Lorg/junit/Test;";
    private static final String TEST_CLASS_PLACEHOLDER = "TEST_CLASS";
    private static final String TEST_METHOD_NAME_PLACEHOLDER = "TEST_METHOD_NAME";
    private final InputClassRepository repository;

    public JU4BenchmarkFactory(InputClassRepository repository) {
        this.repository = repository;
    }

    private static CompilationUnit classTemplate() {
        return CLASS_TEMPLATE.clone();
    }

    private static MethodDeclaration methodTemplate() {
        return METHOD_TEMPLATE.clone();
    }

    private static Predicate<Method> isJUnit4Test() {
        return m -> Arrays.stream(m.getAnnotationEntries())
                .anyMatch(a -> a.getAnnotationType().equals(J_UNIT_4_TEST_ANNOTATION));
    }

    private Stream<String> findTestMethods(JavaClass bytecode) {
        Stream<String> declaredTestMethods = Arrays.stream(bytecode.getMethods())
                .filter(isJUnit4Test())
                .map(FieldOrMethod::getName);
        InputClass superclass;
        try {
            superclass = repository.findClass(bytecode.getSuperclassName());
            return Stream.concat(findTestMethods(superclass.getBytecode()), declaredTestMethods)
                    .distinct();
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
        CompilationUnit output = classTemplate();
        if (packageName != null) {
            output.setPackageDeclaration(packageName);
        }
        TypeDeclaration<?> outputClass = output.getType(0);
        outputClass.setName(benchmarkClassName);
        Expression testClassExpression = StaticJavaParser.parseExpression(
                testClassName + ".class");
        outputClass.accept(
                expressionReplacementVisitor(TEST_CLASS_PLACEHOLDER, testClassExpression), null);
        MethodDeclaration methodTemplate = methodTemplate();
        for (String testMethodName : testMethodNames) {
            MethodDeclaration benchmarkMethod = methodTemplate.clone();
            benchmarkMethod.setName("benchmark_" + testMethodName);
            benchmarkMethod.accept(expressionReplacementVisitor(
                    TEST_METHOD_NAME_PLACEHOLDER, new StringLiteralExpr(testMethodName)), null);
            outputClass.addMember(benchmarkMethod);
        }
        return output;
    }

    public CompilationUnit createBenchmarkFromTest(String testClassName)
            throws ClassNotFoundException, InvalidInputClassException {
        InputClass inputClass = repository.findClass(testClassName);
        TypeDeclaration<?> source = inputClass.getSource();
        JavaClass bytecode = inputClass.getBytecode();
        if (bytecode.isAbstract() || bytecode.isInterface()) {
            throw new InvalidInputClassException("Input class" + testClassName
                    + " is abstract or an interface.");
        }
        String packageName = source.findCompilationUnit()
                .orElseThrow()
                .getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .orElse(null);
        String testClassShortCanonicalName = ClassNames.shortClassName(testClassName)
                .replace('$', '.');
        String benchmarkClassName =
                testClassShortCanonicalName.replace('.', '_') + "_JU4Benchmark";
        List<String> testMethodNames = findTestMethods(bytecode)
                .collect(Collectors.toUnmodifiableList());
        if (testMethodNames.isEmpty()) {
            throw new InvalidInputClassException(
                    "Found no test methods for input class " + testClassName + ".");
        }
        return generateBenchmark(
                packageName, benchmarkClassName, testClassShortCanonicalName, testMethodNames);
    }
}
