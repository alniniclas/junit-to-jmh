package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.output.StringBuilderWriter;
import se.chalmers.ju2jmh.model.FixtureMethod;
import se.chalmers.ju2jmh.model.TestRule;
import se.chalmers.ju2jmh.model.UnitTest;
import se.chalmers.ju2jmh.model.UnitTestClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A factory for tailored benchmarks.
 */
public class TailoredBenchmarkFactory {
    private static final Configuration templateConfig = buildTemplateConfig();

    private static Configuration buildTemplateConfig() {
        Configuration config = new Configuration(Configuration.VERSION_2_3_29);
        config.setClassLoaderForTemplateLoading(
                TailoredBenchmarkFactory.class.getClassLoader(), "templates/tailored");
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setLogTemplateExceptions(false);
        config.setWrapUncheckedExceptions(true);
        config.setFallbackOnNullLoopVariable(false);
        // Disable updates
        config.setTemplateUpdateDelayMilliseconds(Long.MAX_VALUE);
        return config;
    }

    private TailoredBenchmarkFactory() {
        throw new AssertionError("Should not be instantiated.");
    }

    /**
     * Generates a tailored benchmark class for the given unit test class, using the given name
     * validator to prevent naming conflicts.
     *
     * @param testClass {@link UnitTestClass} representing the class to generate benchmarks for
     * @param nameValidator predicate for validating identifier names, to avoid name clashes
     * @return the generated benchmark class
     */
    public static ClassOrInterfaceDeclaration generateBenchmarkClass(
            UnitTestClass testClass, Predicate<String> nameValidator) {
        Template template;
        try {
            template = templateConfig.getTemplate("tailored_benchmark.ftl");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load benchmark template.", e);
        }
        String output;
        try (StringBuilderWriter writer = new StringBuilderWriter()) {
            Model.BenchmarkClass model = Model.generate(testClass, nameValidator);
            template.process(model, writer);
            output = writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
        return StaticJavaParser.parseTypeDeclaration(output).asClassOrInterfaceDeclaration();
    }

    /**
     * Generates a tailored benchmark class for the given unit test class, without checking for
     * naming conflicts.
     *
     * @param testClass {@link UnitTestClass} representing the class to generate benchmarks for
     * @return the generated benchmark class
     */
    public static ClassOrInterfaceDeclaration generateBenchmarkClass(UnitTestClass testClass) {
        return generateBenchmarkClass(testClass, n -> true);
    }

    /**
     * Creates a name validator for the given compilation unit. The created name validator will only
     * allow names that are not already present as identifiers in the given compilation unit.
     *
     * @param compilationUnit the compilation unit to scan for identifiers
     * @return a predicate returning false if the name it is called with is already present as an
     * identifier in the given compilation unit
     */
    public static Predicate<String> nameValidatorForCompilationUnit(
            CompilationUnit compilationUnit) {
        Set<String> names = new HashSet<>();
        compilationUnit.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(SimpleName n, Void arg) {
                names.add(n.getIdentifier());
                super.visit(n, arg);
            }

            @Override
            public void visit(Name n, Void arg) {
                names.add(n.getIdentifier());
                super.visit(n, arg);
            }

            @Override
            public void visit(MethodReferenceExpr n, Void arg) {
                names.add(n.getIdentifier());
                super.visit(n, arg);
            }
        }, null);
        return Predicate.not(names::contains);
    }

    private static String getValidName(String preferred, Predicate<String> nameValidator) {
        if (nameValidator.test(preferred)) {
            return preferred;
        }
        String baseName = preferred + "_";
        return IntStream.iterate(0, i -> i + 1)
                .mapToObj(i -> baseName + i)
                .filter(nameValidator)
                .findFirst()
                .orElseThrow();
    }

    private static class Member<M> {
        public final M member;
        public final String declaringClass;

        private Member(M member, String declaringClass) {
            this.member = member;
            this.declaringClass = declaringClass;
        }
    }

    private static <M> Stream<Member<M>> extractMembers(
            UnitTestClass testClass, Function<UnitTestClass, Stream<M>> getMembers,
            boolean superFirst) {
        Stream<Member<M>> members = getMembers.apply(testClass)
                .map(m -> new Member<>(m, testClass.name()));
        if (testClass.superclass().isEmpty()) {
            return members;
        }
        Stream<Member<M>> superMembers =
                extractMembers(testClass.superclass().get(), getMembers, superFirst);
        return superFirst
                ? Stream.concat(superMembers, members)
                : Stream.concat(members, superMembers);
    }

    private static <M> List<Member<M>> deduplicate(
            List<Member<M>> members, Function<M, String> getName, boolean keepFirst) {
        if (!keepFirst) {
            return Lists.reverse(deduplicate(Lists.reverse(members), getName, true));
        }
        Set<String> names = new HashSet<>();
        List<Member<M>> deduplicated = new ArrayList<>();
        for (Member<M> member : members) {
            String name = getName.apply(member.member);
            if (!names.contains(name)) {
                names.add(name);
                deduplicated.add(member);
            }
        }
        return Collections.unmodifiableList(deduplicated);
    }

    private static <M> Stream<Member<M>> deduplicate(
            Stream<Member<M>> members, Function<M, String> getName, boolean keepFirst) {
        return deduplicate(members.collect(Collectors.toUnmodifiableList()), getName, keepFirst)
                .stream();
    }

    private static Stream<Member<UnitTest>> extractTests(UnitTestClass testClass) {
        Stream<Member<UnitTest>> tests = extractMembers(testClass, t -> t.tests().stream(), true);
        return deduplicate(tests, UnitTest::name, false);
    }

    private static Stream<Member<FixtureMethod>> extractBefore(UnitTestClass testClass) {
        Stream<Member<FixtureMethod>> tests = extractMembers(testClass, t -> t.before().stream(), true);
        return deduplicate(tests, FixtureMethod::name, false);
    }

    private static Stream<Member<FixtureMethod>> extractAfter(UnitTestClass testClass) {
        Stream<Member<FixtureMethod>> tests = extractMembers(testClass, t -> t.after().stream(), false);
        return deduplicate(tests, FixtureMethod::name, true);
    }

    private static Stream<Member<FixtureMethod>> extractBeforeClass(UnitTestClass testClass) {
        return extractMembers(testClass, t -> t.beforeClass().stream(), true);
    }

    private static Stream<Member<FixtureMethod>> extractAfterClass(UnitTestClass testClass) {
        return extractMembers(testClass, t -> t.afterClass().stream(), false);
    }

    private static Stream<Member<TestRule>> extractInstanceRules(UnitTestClass testClass) {
        Stream<Member<TestRule>> ruleFields =
                extractMembers(testClass, t -> t.ruleFields().stream(), false);
        Stream<Member<TestRule>> ruleMethods =
                extractMembers(testClass, t -> t.ruleMethods().stream(), false);
        return Stream.concat(ruleFields, deduplicate(ruleMethods, TestRule::name, true));
    }

    private static Stream<Member<TestRule>> extractClassRules(UnitTestClass testClass) {
        Stream<Member<TestRule>> ruleFields =
                extractMembers(testClass, t -> t.classRuleFields().stream(), false);
        Stream<Member<TestRule>> ruleMethods =
                extractMembers(testClass, t -> t.classRuleMethods().stream(), false);
        return Stream.concat(ruleFields, ruleMethods);
    }

    private static class Model {
        private Model() {
            throw new AssertionError("Should not be instantiated");
        }

        public static class BenchmarkClass {
            private final String className;
            private final String testClassName;
            private final String instanceStatementClassName;
            private final String classStatementClassName;
            private final String applyClassRulesStatementClassName;
            private final String payloadsClassName;
            private final List<Benchmark> benchmarks;
            private final List<ClassFixtureMethod> beforeClassMethods;
            private final List<ClassFixtureMethod> afterClassMethods;
            private final List<InstanceFixtureMethod> beforeMethods;
            private final List<InstanceFixtureMethod> afterMethods;
            private final List<ClassRule> classRules;
            private final List<InstanceRule> instanceRules;

            public BenchmarkClass(
                    String className, String testClassName, String instanceStatementClassName,
                    String classStatementClassName, String applyClassRulesStatementClassName,
                    String payloadsClassName, List<Benchmark> benchmarks,
                    List<ClassFixtureMethod> beforeClassMethods,
                    List<ClassFixtureMethod> afterClassMethods,
                    List<InstanceFixtureMethod> beforeMethods,
                    List<InstanceFixtureMethod> afterMethods, List<ClassRule> classRules,
                    List<InstanceRule> instanceRules) {
                this.className = className;
                this.testClassName = testClassName;
                this.instanceStatementClassName = instanceStatementClassName;
                this.classStatementClassName = classStatementClassName;
                this.applyClassRulesStatementClassName = applyClassRulesStatementClassName;
                this.payloadsClassName = payloadsClassName;
                this.benchmarks = benchmarks;
                this.beforeClassMethods = beforeClassMethods;
                this.afterClassMethods = afterClassMethods;
                this.beforeMethods = beforeMethods;
                this.afterMethods = afterMethods;
                this.classRules = classRules;
                this.instanceRules = instanceRules;
            }

            public String getClassName() {
                return className;
            }

            public String getPayloadsClassName() {
                return payloadsClassName;
            }

            public String getTestClassName() {
                return testClassName;
            }

            public String getInstanceStatementClassName() {
                return instanceStatementClassName;
            }

            public String getClassStatementClassName() {
                return classStatementClassName;
            }

            public String getApplyClassRulesStatementClassName() {
                return applyClassRulesStatementClassName;
            }

            public List<Benchmark> getBenchmarks() {
                return benchmarks;
            }

            public boolean getHasClassRules() {
                return !classRules.isEmpty();
            }

            public boolean getHasInstanceRules() {
                return !instanceRules.isEmpty();
            }

            public boolean getHasRules() {
                return getHasClassRules() || getHasInstanceRules();
            }

            public List<ClassFixtureMethod> getBeforeClassMethods() {
                return beforeClassMethods;
            }

            public List<ClassFixtureMethod> getAfterClassMethods() {
                return afterClassMethods;
            }

            public List<InstanceFixtureMethod> getBeforeMethods() {
                return beforeMethods;
            }

            public List<InstanceFixtureMethod> getAfterMethods() {
                return afterMethods;
            }

            public List<ClassRule> getClassRules() {
                return classRules;
            }

            public List<InstanceRule> getInstanceRules() {
                return instanceRules;
            }
        }

        public static class Benchmark {
            private final String testName;
            private final String expectedException;

            public Benchmark(String testName, String expectedException) {
                this.testName = testName;
                this.expectedException = expectedException;
            }

            public Benchmark(String testName) {
                this(testName, null);
            }

            public String getTestName() {
                return testName;
            }

            public String getExpectedException() {
                return expectedException;
            }

            public boolean isExceptionTest() {
                return expectedException != null;
            }
        }

        public static class ClassFixtureMethod {
            private final String name;
            private final String className;

            public ClassFixtureMethod(String name, String className) {
                this.name = name;
                this.className = className;
            }

            public String getName() {
                return name;
            }

            public String getClassName() {
                return className;
            }
        }

        public static class InstanceFixtureMethod {
            private final String name;

            public InstanceFixtureMethod(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }

        public static class ClassRule {
            private final String name;
            private final String className;
            private final boolean fromField;

            public ClassRule(String name, String className, boolean fromField) {
                this.name = name;
                this.className = className;
                this.fromField = fromField;
            }

            public String getName() {
                return name;
            }

            public String getClassName() {
                return className;
            }

            public boolean isFromField() {
                return fromField;
            }

            public boolean isFromMethod() {
                return !isFromField();
            }
        }

        public static class InstanceRule {
            private final String name;
            private final String className;
            private final boolean fromField;
            private final boolean hidden;

            public InstanceRule(String name, String className, boolean fromField, boolean hidden) {
                this.name = name;
                this.className = className;
                this.fromField = fromField;
                this.hidden = hidden;
            }

            public String getName() {
                return name;
            }

            public String getClassName() {
                return className;
            }

            public boolean isFromField() {
                return fromField;
            }

            public boolean isFromMethod() {
                return !isFromField();
            }

            public boolean isHidden() {
                return hidden;
            }
        }

        public static BenchmarkClass generate(UnitTestClass testClass, Predicate<String> nameValidator) {
            String benchmarkClassName = getValidName("_Benchmark", nameValidator);
            String instanceStatementClassName = getValidName("_InstanceStatement", nameValidator);
            String classStatementClassName = getValidName("_ClassStatement", nameValidator);
            String applyClassRulesStatementClassName =
                    getValidName("_ApplyClassRulesStatement", nameValidator);
            String payloadsClassName = getValidName("_Payloads", nameValidator);
            List<Benchmark> benchmarks = extractTests(testClass)
                    .map(t -> t.member)
                    .map(t -> new Benchmark(
                            t.name(), t.expectedException().orElse(null)))
                    .collect(Collectors.toUnmodifiableList());
            Function<String, String> classNameReference =
                    n -> n.equals(testClass.name()) ? ClassNames.shortClassName(n) : n;
            List<ClassFixtureMethod> beforeClassMethods = extractBeforeClass(testClass)
                    .map(b -> new ClassFixtureMethod(
                            b.member.name(),
                            classNameReference.apply(b.declaringClass)))
                    .collect(Collectors.toUnmodifiableList());
            List<ClassFixtureMethod> afterClassMethods = extractAfterClass(testClass)
                    .map(a -> new ClassFixtureMethod(
                            a.member.name(),
                            classNameReference.apply(a.declaringClass)))
                    .collect(Collectors.toUnmodifiableList());
            List<InstanceFixtureMethod> beforeMethods = extractBefore(testClass)
                    .map(b -> new InstanceFixtureMethod(b.member.name()))
                    .collect(Collectors.toUnmodifiableList());
            List<InstanceFixtureMethod> afterMethods = extractAfter(testClass)
                    .map(a -> new InstanceFixtureMethod(a.member.name()))
                    .collect(Collectors.toUnmodifiableList());
            List<ClassRule> classRules = extractClassRules(testClass)
                    .map(r -> new ClassRule(
                            r.member.name(),
                            classNameReference.apply(r.declaringClass),
                            r.member.source() == TestRule.Source.FIELD))
                    .collect(Collectors.toUnmodifiableList());
            List<Member<TestRule>> rawInstanceRules =
                    extractInstanceRules(testClass).collect(Collectors.toUnmodifiableList());
            List<InstanceRule> instanceRules = new ArrayList<>();
            Set<String> seenFieldNames = new HashSet<>();
            for (Member<TestRule> testRule : rawInstanceRules) {
                instanceRules.add(new InstanceRule(
                        testRule.member.name(), classNameReference.apply(testRule.declaringClass),
                        testRule.member.source() == TestRule.Source.FIELD,
                        seenFieldNames.contains(testRule.member.name())));
                seenFieldNames.add(testRule.member.name());
            }
            instanceRules = Collections.unmodifiableList(instanceRules);
            return new BenchmarkClass(
                    benchmarkClassName, ClassNames.shortClassName(testClass.name()),
                    instanceStatementClassName, classStatementClassName,
                    applyClassRulesStatementClassName, payloadsClassName, benchmarks,
                    beforeClassMethods, afterClassMethods, beforeMethods, afterMethods, classRules,
                    instanceRules);
        }
    }
}
