package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.LinkedHashMap;
import java.util.Map;

public class WrapperBenchmarkFactory {

    private static class TestLocation {
        final String binaryClassName;
        final String methodName;

        TestLocation(String binaryClassName, String methodName) {
            this.binaryClassName = binaryClassName;
            this.methodName = methodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestLocation that = (TestLocation) o;
            return binaryClassName.equals(that.binaryClassName) && methodName.equals(that.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(binaryClassName, methodName);
        }
    }

    private static final String J_UNIT_4_TEST_ANNOTATION = "Lorg/junit/Test;";
    private static final String J_UNIT_5_TEST_ANNOTATION = "Lorg/junit/jupiter/api/Test;";
    private static final String TEST_CLASS_PLACEHOLDER = "TEST_CLASS";
    private static final String TEST_METHOD_NAME_PLACEHOLDER = "TEST_METHOD_NAME";
    private static final String TEST_CLASS_NAME_PLACEHOLDER = "TEST_CLASS_NAME"; // Nuovo placeholder

    private final InputClassRepository repository;

    public WrapperBenchmarkFactory(InputClassRepository repository) {
        this.repository = repository;
    }

    private boolean isJUnit5(CompilationUnit source) {
        if (source.getImports() == null) return false;
        return source.getImports().stream()
                .map(ImportDeclaration::getNameAsString)
                .anyMatch(name -> name.startsWith("org.junit.jupiter"));
    }

    private CompilationUnit getClassTemplate(boolean isJu5) {
        String path = isJu5 ? "templates/ju5_benchmark/class_template.java"
                : "templates/ju4_benchmark/class_template.java";
        return AstTemplates.compilationUnit(path).clone();
    }

    private MethodDeclaration getMethodTemplate(boolean isJu5) {
        String path = isJu5 ? "templates/ju5_benchmark/method_template.java"
                : "templates/ju4_benchmark/method_template.java";
        return AstTemplates.method(path).clone();
    }

    private static Predicate<Method> isJUnitTest() {
        return m -> Arrays.stream(m.getAnnotationEntries())
                .anyMatch(a -> a.getAnnotationType().equals(J_UNIT_4_TEST_ANNOTATION)
                        || a.getAnnotationType().equals(J_UNIT_5_TEST_ANNOTATION));
    }


    private Stream<TestLocation> findTestMethods(JavaClass bytecode) {
        List<String> classChain = new ArrayList<>();
        JavaClass current = bytecode;
        try {
            while (current != null && !"java.lang.Object".equals(current.getClassName())) {
                classChain.add(current.getClassName());
                String sc = current.getSuperclassName();
                if (sc == null || sc.equals("java.lang.Object")) break;
                try {
                    current = repository.findClass(sc).getBytecode();
                } catch (ClassNotFoundException e) {
                    break;
                }
            }
        } catch (Exception e) {
        }

        java.util.Collections.reverse(classChain);

        Map<String, TestLocation> byMethodName = new LinkedHashMap<>();

        for (String className : classChain) {
            try {
                JavaClass jc = repository.findClass(className).getBytecode();
                Arrays.stream(jc.getMethods())
                        .filter(isJUnitTest())
                        .forEach(m -> {
                            byMethodName.put(m.getName(), new TestLocation(jc.getClassName(), m.getName()));
                        });
            } catch (ClassNotFoundException ignored) {

            }
        }

        return byMethodName.values().stream();
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
            String packageName, String benchmarkClassName, String topLevelClassName,
            List<TestLocation> locations, CompilationUnit classTemplate, MethodDeclaration methodTemplate, boolean isJu5) {

        CompilationUnit output = classTemplate.clone();
        if (packageName != null) output.setPackageDeclaration(packageName);

        TypeDeclaration<?> outputClass = output.getType(0);
        outputClass.setName(benchmarkClassName);


        Expression testClassExpression = StaticJavaParser.parseExpression(topLevelClassName + ".class");
        outputClass.accept(expressionReplacementVisitor(TEST_CLASS_PLACEHOLDER, testClassExpression), null);

        for (TestLocation loc : locations) {
            MethodDeclaration benchmarkMethod = methodTemplate.clone();

            benchmarkMethod.setName("benchmark_" + loc.methodName);

            benchmarkMethod.accept(expressionReplacementVisitor(TEST_METHOD_NAME_PLACEHOLDER, new StringLiteralExpr(loc.methodName)), null);

            if (isJu5) {
                benchmarkMethod.accept(expressionReplacementVisitor(TEST_CLASS_NAME_PLACEHOLDER, new StringLiteralExpr(loc.binaryClassName)), null);
            }

            outputClass.addMember(benchmarkMethod);
        }
        return output;
    }

    public CompilationUnit createBenchmarkFromTest(String testClassName)
            throws ClassNotFoundException, InvalidInputClassException {

        InputClass inputClass = repository.findClass(testClassName);
        JavaClass bytecode = inputClass.getBytecode();
        CompilationUnit sourceCu = inputClass.getSource().findCompilationUnit().orElseThrow();

        if (bytecode.isAbstract() || bytecode.isInterface()) {
            throw new InvalidInputClassException("Input class " + testClassName + " is abstract or interface.");
        }

        String packageName = sourceCu.getPackageDeclaration().map(NodeWithName::getNameAsString).orElse(null);
        boolean isJu5 = isJUnit5(sourceCu);

        String topLevelShortName = ClassNames.shortClassName(testClassName).replace('$', '.');
        String benchmarkClassName = topLevelShortName.replace('.', '_') + (isJu5 ? "_JU5Benchmark" : "_JU4Benchmark");

        List<TestLocation> locations = findTestMethods(bytecode).collect(Collectors.toList());

        if (locations.isEmpty()) {
            throw new InvalidInputClassException("Found no test methods for " + testClassName);
        }

        return generateBenchmark(packageName, benchmarkClassName, topLevelShortName, locations,
                getClassTemplate(isJu5), getMethodTemplate(isJu5), isJu5);
    }
}
