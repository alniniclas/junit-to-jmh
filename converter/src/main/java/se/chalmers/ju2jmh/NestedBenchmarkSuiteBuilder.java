package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithStaticModifier;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.google.common.collect.Iterables;
import org.apache.bcel.classfile.AccessFlags;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NestedBenchmarkSuiteBuilder {
    private static final ClassOrInterfaceDeclaration BENCHMARK_CLASS_TEMPLATE =
            AstTemplates.type("templates/nested_benchmark/benchmark_class_template.java")
                    .asClassOrInterfaceDeclaration();
    private final List<Path> sourcePath;
    private final InputClassRepository inputClassRepository;
    private final Map<String, InputClass> benchmarkClasses = new HashMap<>();
    private final Map<String, InputClass> abstractBenchmarkClasses = new HashMap<>();

    public NestedBenchmarkSuiteBuilder(List<Path> sourcePaths, List<Path> classPath) {
        this.sourcePath = sourcePaths.stream().collect(Collectors.toUnmodifiableList());
        this.inputClassRepository = new InputClassRepository(sourcePaths, classPath);
    }

    public NestedBenchmarkSuiteBuilder(Path sourcePath, Path classPath) {
        this.sourcePath = List.of(sourcePath);
        this.inputClassRepository = new InputClassRepository(sourcePath, classPath);
    }

    private boolean isTestClass(InputClass inputClass) {
        JavaClass bytecode = inputClass.getBytecode();
        if (bytecode.isInterface() || bytecode.isEnum() || bytecode.isAnnotation()) {
            return false;
        }
        boolean hasTestMethods = Arrays.stream(bytecode.getMethods())
                .filter(AccessFlags::isPublic)
                .filter(Bytecode.Predicates.hasArgCount(0))
                .anyMatch(Bytecode.Predicates.isMethodAnnotated(Test.class)
                        .or(Bytecode.Predicates.isMethodAnnotated(Before.class))
                        .or(Bytecode.Predicates.isMethodAnnotated(After.class))
                        .or(Bytecode.Predicates.isMethodAnnotated(BeforeClass.class))
                        .or(Bytecode.Predicates.isMethodAnnotated(AfterClass.class))
                        .or(Bytecode.Predicates.isMethodAnnotated(Rule.class))
                        .or(Bytecode.Predicates.isMethodAnnotated(ClassRule.class)));
        if (hasTestMethods) {
            return true;
        }
        return Arrays.stream(bytecode.getFields())
                .filter(AccessFlags::isPublic)
                .anyMatch(Bytecode.Predicates.isFieldAnnotated(Rule.class)
                        .or(Bytecode.Predicates.isFieldAnnotated(ClassRule.class)));
    }

    private void addAbstractTestClass(String className) throws ClassNotFoundException {
        if (className.equals(Object.class.getName()) || benchmarkClasses.containsKey(className)
                || abstractBenchmarkClasses.containsKey(className)) {
            return;
        }
        InputClass inputClass = inputClassRepository.findClass(className);
        String superclassName = inputClass.getSuperclassName();
        try {
            addAbstractTestClass(superclassName);
        } catch (ClassNotFoundException e) {
            // Superclass is unavailable. Assume it is a non-test class and continue.
        }
        if (!benchmarkClasses.containsKey(superclassName)
                && !abstractBenchmarkClasses.containsKey(superclassName)) {
            // Superclass is not test class, so check if this one should be added.
            if (isTestClass(inputClass)) {
                abstractBenchmarkClasses.put(className, inputClass);
            }
        } else {
            // Superclass is test class, so add regardless.
            abstractBenchmarkClasses.put(className, inputClass);
        }
    }

    public NestedBenchmarkSuiteBuilder addTestClass(String className)
            throws ClassNotFoundException {
        addAbstractTestClass(className);
        InputClass inputClass = abstractBenchmarkClasses.get(className);
        if (inputClass != null && !inputClass.getBytecode().isAbstract()) {
            abstractBenchmarkClasses.remove(className);
            benchmarkClasses.put(className, inputClass);
        }
        return this;
    }

    public NestedBenchmarkSuiteBuilder addTestClassIncludingNested(String className)
            throws ClassNotFoundException {
        addTestClass(className);
        InputClass inputClass = inputClassRepository.findClass(className);
        for (BodyDeclaration<?> member : inputClass.getSource().getMembers()) {
            if (member.isTypeDeclaration()) {
                if (member instanceof NodeWithStaticModifier
                        && !((NodeWithStaticModifier<?>) member).isStatic()) {
                    continue;
                }
                addTestClassIncludingNested(
                        className + "$" + member.asTypeDeclaration().getNameAsString());
            }
        }
        return this;
    }

    private static final class BenchmarkTemplateModifier extends ModifierVisitor<InputClass> {
        private static final Expression ACCESS_IMPLEMENTATION =
                StaticJavaParser.parseExpression("this.implementation()");
        private static final MethodCallExpr RULE_APPLICATION_CALL =
                StaticJavaParser.parseExpression("this.applyRule(RULE, statement, description)");
        private static final AssignExpr RULE_APPLICATION_ASSIGNMENT =
                StaticJavaParser.parseExpression("statement = NEW_STATEMENT");
        private static final MethodDeclaration BENCHMARK_METHOD =
                AstTemplates.method("templates/nested_benchmark/benchmark_method_template.java");
        private static final MethodDeclaration EXCEPTION_BENCHMARK_METHOD = AstTemplates.method(
                "templates/nested_benchmark/exception_benchmark_method_template.java");

        @Override
        public Visitable visit(ClassOrInterfaceType n, InputClass arg) {
            if (n.getNameAsString().equals("IMPLEMENTATION_CLASS_NAME")) {
                return StaticJavaParser.parseType(ClassNames.simpleClassName(arg.getName()));
            }
            return super.visit(n, arg);
        }

        private static Expression classNameExpression(InputClass arg) {
            return StaticJavaParser.parseExpression(
                    ClassNames.simpleClassName(arg.getName()));
        }

        private static Stream<MethodCallExpr> instanceMethodCalls(Predicate<Method> filter,
                InputClass inputClass) {
            return Arrays.stream(inputClass.getBytecode().getMethods())
                    .filter(AccessFlags::isPublic)
                    .filter(Predicate.not(AccessFlags::isStatic))
                    .filter(Bytecode.Predicates.hasArgCount(0))
                    .filter(filter)
                    .map(FieldOrMethod::getName)
                    .map(mn -> new MethodCallExpr(mn).setScope(ACCESS_IMPLEMENTATION.clone()));
        }

        private static Stream<MethodCallExpr> staticMethodCalls(Predicate<Method> filter,
                InputClass inputClass) {
            return Arrays.stream(inputClass.getBytecode().getMethods())
                    .filter(AccessFlags::isPublic)
                    .filter(AccessFlags::isStatic)
                    .filter(Bytecode.Predicates.hasArgCount(0))
                    .filter(filter)
                    .map(FieldOrMethod::getName)
                    .map(mn -> new MethodCallExpr(mn).setScope(classNameExpression(inputClass)));
        }

        private enum MemberType {
            STATIC_METHOD, INSTANCE_METHOD, STATIC_FIELD, INSTANCE_FIELD
        }

        private enum SuperCallOrder {
            FIRST, LAST
        }

        private static MethodDeclaration populateFixtureMethod(MethodDeclaration method,
                InputClass arg, Class<? extends Annotation> annotation, MemberType methodType,
                SuperCallOrder callSuper) {
            BlockStmt body = method.getBody().orElseThrow();
            Statement superCall = callSuper == SuperCallOrder.LAST
                    ? body.getStatements().remove(0)
                    : null;
            switch (methodType) {
                case STATIC_METHOD:
                    staticMethodCalls(Bytecode.Predicates.isMethodAnnotated(annotation), arg)
                            .forEach(body::addStatement);
                    break;
                case INSTANCE_METHOD:
                    instanceMethodCalls(Bytecode.Predicates.isMethodAnnotated(annotation), arg)
                            .forEach(body::addStatement);
                    break;
                default:
                    throw new AssertionError(
                            "methodType should always be STATIC_METHOD or INSTANCE_METHOD");
            }
            if (callSuper == SuperCallOrder.LAST) {
                body.addStatement(superCall);
            }
            if (body.getStatements().size() > 1) {
                return method;
            } else {
                return null;
            }
        }

        private static Stream<FieldAccessExpr> fieldRules(InputClass arg, MemberType memberType) {
            Predicate<Field> predicate;
            Expression scope;
            switch (memberType) {
                case STATIC_FIELD:
                    predicate = Bytecode.Predicates.isFieldAnnotated(ClassRule.class)
                            .and(AccessFlags::isStatic);
                    scope = classNameExpression(arg);
                    break;
                case INSTANCE_FIELD:
                    predicate = Bytecode.Predicates.isFieldAnnotated(Rule.class)
                            .and(Predicate.not(AccessFlags::isStatic));
                    scope = ACCESS_IMPLEMENTATION;
                    break;
                default:
                    throw new AssertionError(
                            "memberType should always be STATIC_FIELD or INSTANCE_FIELD");
            }
            return Arrays.stream(arg.getBytecode().getFields())
                    .filter(AccessFlags::isPublic)
                    .filter(predicate)
                    .map(FieldOrMethod::getName)
                    .map(fn -> new FieldAccessExpr(scope.clone(), fn));
        }

        public static MethodDeclaration populateApplyRulesMethod(MethodDeclaration apply,
                InputClass arg, MemberType memberType) {
            BlockStmt body = apply.getBody().orElseThrow();
            Statement superCall = body.getStatement(0);
            Statement returnStatement = body.getStatement(1);
            body.remove(superCall);
            body.remove(returnStatement);
            Stream<? extends Expression> rules;
            switch (memberType) {
                case STATIC_FIELD:
                case INSTANCE_FIELD:
                    rules = fieldRules(arg, memberType);
                    break;
                case STATIC_METHOD:
                    rules = staticMethodCalls(
                            Bytecode.Predicates.isMethodAnnotated(ClassRule.class), arg);
                    break;
                case INSTANCE_METHOD:
                    rules = instanceMethodCalls(Bytecode.Predicates.isMethodAnnotated(Rule.class),
                            arg);
                    break;
                default:
                    throw new AssertionError(
                            "memberType should always be STATIC_FIELD, INSTANCE_FIELD, "
                                    + "STATIC_METHOD, or INSTANCE_METHOD");
            }
            rules.map(mc -> RULE_APPLICATION_CALL.clone().setArgument(0, mc))
                    .map(mc -> RULE_APPLICATION_ASSIGNMENT.clone().setValue(mc))
                    .forEach(body::addStatement);
            body.addStatement(superCall);
            body.addStatement(returnStatement);
            if (body.getStatements().size() > 2) {
                return apply;
            } else {
                return null;
            }
        }

        @Override
        public Visitable visit(MethodDeclaration n, InputClass arg) {
            switch (n.getNameAsString()) {
                case "beforeClass":
                    return populateFixtureMethod(n, arg, BeforeClass.class,
                            MemberType.STATIC_METHOD, SuperCallOrder.FIRST);
                case "afterClass":
                    return populateFixtureMethod(n, arg, AfterClass.class, MemberType.STATIC_METHOD,
                            SuperCallOrder.LAST);
                case "before":
                    return populateFixtureMethod(n, arg, Before.class, MemberType.INSTANCE_METHOD,
                            SuperCallOrder.FIRST);
                case "after":
                    return populateFixtureMethod(n, arg, After.class, MemberType.INSTANCE_METHOD,
                            SuperCallOrder.LAST);
                case "applyClassRuleFields":
                    return populateApplyRulesMethod(n, arg, MemberType.STATIC_FIELD);
                case "applyClassRuleMethods":
                    return populateApplyRulesMethod(n, arg, MemberType.STATIC_METHOD);
                case "applyRuleFields":
                    return populateApplyRulesMethod(n, arg, MemberType.INSTANCE_FIELD);
                case "applyRuleMethods":
                    return populateApplyRulesMethod(n, arg, MemberType.INSTANCE_METHOD);
                case "implementation":
                case "createImplementation":
                    if (arg.getBytecode().isAbstract()) {
                        return super.visit(n.removeBody().setAbstract(true), arg);
                    }
                    break;
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(FieldDeclaration n, InputClass arg) {
            if (n.getVariables()
                    .stream()
                    .map(NodeWithSimpleName::getNameAsString)
                    .anyMatch(Predicate.isEqual("implementation"))
                    && arg.getBytecode().isAbstract()) {
                return null;
            }
            return super.visit(n, arg);
        }

        private static Optional<String> getExpectedException(Method method) {
            return Bytecode.getAnnotation(method, Test.class)
                    .stream()
                    .map(AnnotationEntry::getElementValuePairs)
                    .flatMap(Arrays::stream)
                    .filter(evp -> evp.getNameString().equals("expected"))
                    .map(ElementValuePair::getValue)
                    .map(ElementValue::stringifyValue)
                    .map(Bytecode::referenceFieldTypeDescriptorToClassName)
                    .filter(Predicate.not(Predicate.isEqual(Test.None.class.getName())))
                    .findFirst();
        }

        private static MethodDeclaration generateBenchmarkMethod(Method method) {
            Optional<String> expectedException = getExpectedException(method);
            MethodDeclaration benchmarkMethod = expectedException.isEmpty()
                    ? BENCHMARK_METHOD.clone()
                    : EXCEPTION_BENCHMARK_METHOD.clone();
            benchmarkMethod.setName("benchmark_" + method.getName());
            BlockStmt body = benchmarkMethod.getBody().orElseThrow();
            MethodCallExpr runBenchmarkCall = body.getStatement(1)
                    .asExpressionStmt()
                    .getExpression()
                    .asMethodCallExpr();
            runBenchmarkCall.getArgument(0)
                    .asMethodReferenceExpr()
                    .setIdentifier(method.getName());
            runBenchmarkCall.getArgument(1)
                    .asMethodCallExpr()
                    .getArgument(0)
                    .asStringLiteralExpr()
                    .setString(method.getName());
            expectedException.ifPresent(ex ->
                    runBenchmarkCall.setArgument(2,
                            new ClassExpr(StaticJavaParser.parseType(
                                    ClassNames.canonicalClassName(ex)))));
            return benchmarkMethod;
        }

        private Stream<MethodDeclaration> generateBenchmarkMethods(InputClass arg) {
            return Arrays.stream(arg.getBytecode().getMethods())
                    .filter(AccessFlags::isPublic)
                    .filter(Predicate.not(AccessFlags::isStatic))
                    .filter(Bytecode.Predicates.hasArgCount(0))
                    .filter(Bytecode.Predicates.isMethodAnnotated(Test.class))
                    .filter(Predicate.not(
                            Bytecode.Predicates.isMethodAnnotated(Ignore.class)))
                    .map(BenchmarkTemplateModifier::generateBenchmarkMethod);
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, InputClass arg) {
            Stream<MethodDeclaration> benchmarkMethods = generateBenchmarkMethods(arg);
            n.setMembers(Stream.concat(benchmarkMethods, n.getMembers().stream())
                    .collect(NodeList.toNodeList()));
            return super.visit(n, arg);
        }
    }

    private void loadOutputCompilationUnits(Map<String, CompilationUnit> compilationUnits,
            File directory, String packageName) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                loadOutputCompilationUnits(compilationUnits, file,
                        packageName + "." + file.getName());
            } else if (file.getName().toLowerCase().endsWith(".java")) {
                String className = packageName + "."
                        + file.getName().substring(0, file.getName().length() - ".java".length());
                if (compilationUnits.containsKey(className)) {
                    continue;
                }
                InputClass inputClass = inputClassRepository.findClass(className);
                CompilationUnit compilationUnit = inputClass.getSource()
                        .findCompilationUnit()
                        .orElseThrow(() ->
                                new ClassNotFoundException(
                                        "Failed to load compilation unit for class " + inputClass));
                compilationUnits.put(className, compilationUnit);
            }
        }
    }

    private Map<String, CompilationUnit> loadOutputCompilationUnits()
            throws ClassNotFoundException {
        Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        for (Path path : sourcePath) {
            File file = path.toFile();
            if (file.exists() && file.isDirectory()) {
                File[] packageDirs = file.listFiles();
                if (packageDirs == null) {
                    continue;
                }
                for (File packageDir : packageDirs) {
                    if (packageDir.isDirectory()) {
                        loadOutputCompilationUnits(compilationUnits, packageDir,
                                packageDir.getName());
                    }
                }
            }
        }
        return compilationUnits;
    }

    private TypeDeclaration<?> findTypeInCompilationUnit(String className,
            CompilationUnit compilationUnit) throws ClassNotFoundException {
        String shortClassName = ClassNames.shortClassName(className);
        String outermostClassName = ClassNames.outermostClassName(shortClassName);
        TypeDeclaration<?> outermostType = compilationUnit.getTypes()
                .stream()
                .filter(t -> t.getNameAsString().equals(outermostClassName))
                .findFirst()
                .orElseThrow(() -> new ClassNotFoundException("Failed to find top level class "
                        + ClassNames.outermostClassName(className)
                        + " in compilation unit"));
        if (shortClassName.equals(outermostClassName)) {
            return outermostType;
        } else {
            Queue<String> names = new ArrayDeque<>(Arrays.asList(
                    shortClassName.substring(outermostClassName.length() + 1).split("\\$")));
            TypeDeclaration<?> typeDeclaration = outermostType;
            while (!names.isEmpty()) {
                String name = names.remove();
                typeDeclaration = typeDeclaration.getMembers().stream()
                        .filter(BodyDeclaration::isTypeDeclaration)
                        .map(BodyDeclaration::asTypeDeclaration)
                        .filter(t -> t.getNameAsString().equals(name))
                        .findFirst()
                        .orElseThrow(() -> new ClassNotFoundException(
                                "Failed to find nested type " + className + " in class "
                                        + ClassNames.outermostClassName(className)
                                        + " or its nested classes"));
            }
            return typeDeclaration;
        }
    }

    private TypeDeclaration<?> findOutputTypeDeclaration(
            Map<String, CompilationUnit> compilationUnits, String className)
            throws ClassNotFoundException {
        CompilationUnit compilationUnit =
                compilationUnits.get(ClassNames.outermostClassName(className));
        if (compilationUnit == null) {
            throw new ClassNotFoundException(
                    "Failed to find compilation unit for class " + className);
        }
        return findTypeInCompilationUnit(className, compilationUnit);
    }

    private Stream<String> enclosingTypeNames(TypeDeclaration<?> type) {
        if (!type.isNestedType()) {
            return Stream.empty();
        } else {
            TypeDeclaration<?> enclosing = (TypeDeclaration<?>) type.getParentNode().orElseThrow();
            return Stream.concat(enclosingTypeNames(enclosing),
                    Stream.of(enclosing.getNameAsString()));
        }
    }

    private String benchmarkClassName(TypeDeclaration<?> enclosingType) {
        Stream<String> enclosingTypeNames = Stream.concat(enclosingTypeNames(enclosingType),
                Stream.of(enclosingType.getNameAsString()));
        Stream<String> nestedTypesInEnclosing = enclosingType.getMembers().stream()
                .filter(BodyDeclaration::isTypeDeclaration)
                .map(BodyDeclaration::asTypeDeclaration)
                .map(NodeWithSimpleName::getNameAsString);
        Set<String> existingNames = Stream.concat(enclosingTypeNames, nestedTypesInEnclosing)
                .collect(Collectors.toUnmodifiableSet());
        String benchmarkClassName = "_Benchmark";
        if (existingNames.contains(benchmarkClassName)) {
            String baseName = benchmarkClassName + "_";
            benchmarkClassName = IntStream.iterate(0, i -> i + 1)
                    .mapToObj(i -> baseName + i)
                    .filter(n -> !existingNames.contains(n))
                    .findFirst()
                    .orElseThrow();
        }
        return benchmarkClassName;
    }

    private String name(TypeDeclaration<?> type) {
        if (type.isNestedType()) {
            return name((TypeDeclaration<?>) type.getParentNode().orElseThrow()) + "$"
                    + type.getNameAsString();
        } else {
            return type.getFullyQualifiedName().orElseThrow();
        }
    }

    private InputClass findInputClass(TypeDeclaration<?> type) throws ClassNotFoundException {
        return inputClassRepository.findClass(name(type));
    }

    public Map<String, CompilationUnit> buildSuite() throws ClassNotFoundException {
        Map<String, CompilationUnit> compilationUnits = loadOutputCompilationUnits();
        Map<String, String> benchmarkClassNames = new HashMap<>();
        for (String testClassName : Iterables.concat(benchmarkClasses.keySet(),
                abstractBenchmarkClasses.keySet())) {
            TypeDeclaration<?> enclosing =
                    findOutputTypeDeclaration(compilationUnits, testClassName);
            benchmarkClassNames.put(testClassName, benchmarkClassName(enclosing));
        }
        for (String testClassName : benchmarkClassNames.keySet()) {
            TypeDeclaration<?> enclosing =
                    findOutputTypeDeclaration(compilationUnits, testClassName);
            InputClass testInputClass = findInputClass(enclosing);
            String superclassName =
                    benchmarkClassNames.get(testInputClass.getSuperclassName());
            if (superclassName != null) {
                superclassName = ClassNames.canonicalClassName(
                        testInputClass.getSuperclassName() + "$" + superclassName);
            }
            ClassOrInterfaceDeclaration benchmarkClass = BENCHMARK_CLASS_TEMPLATE.clone();
            benchmarkClass.setName(benchmarkClassNames.get(testClassName));
            if (superclassName != null) {
                benchmarkClass.getExtendedTypes().removeIf(t -> true);
                benchmarkClass.addExtendedType(superclassName);
            }
            if (abstractBenchmarkClasses.containsKey(testClassName)) {
                benchmarkClass.setAbstract(true);
            }
            benchmarkClass.accept(new BenchmarkTemplateModifier(), testInputClass);
            enclosing.addMember(benchmarkClass);
        }
        return Collections.unmodifiableMap(compilationUnits);
    }
}
