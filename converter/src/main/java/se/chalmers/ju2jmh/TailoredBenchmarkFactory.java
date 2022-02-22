package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Lists;
import org.junit.rules.MethodRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import se.chalmers.ju2jmh.api.ExceptionTest;
import se.chalmers.ju2jmh.api.Rules;
import se.chalmers.ju2jmh.api.ThrowingConsumer;
import se.chalmers.ju2jmh.model.FixtureMethod;
import se.chalmers.ju2jmh.model.TestRule;
import se.chalmers.ju2jmh.model.UnitTest;
import se.chalmers.ju2jmh.model.UnitTestClass;

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

    private static boolean hasInstanceRules(UnitTestClass testClass) {
        return !testClass.ruleFields().isEmpty() || !testClass.ruleMethods().isEmpty()
                || (testClass.superclass().isPresent() && hasInstanceRules(
                        testClass.superclass().get()));
    }

    private static boolean hasClassRules(UnitTestClass testClass) {
        return !testClass.classRuleFields().isEmpty() || !testClass.classRuleMethods().isEmpty()
                || (testClass.superclass().isPresent() && hasClassRules(
                        testClass.superclass().get()));
    }

    private static boolean hasRules(UnitTestClass testClass) {
        return hasInstanceRules(testClass) || hasClassRules(testClass);
    }

    private static boolean hasBefore(UnitTestClass testClass) {
        return !testClass.before().isEmpty()
                || (testClass.superclass().isPresent() && hasBefore(testClass.superclass().get()));
    }

    private static boolean hasAfter(UnitTestClass testClass) {
        return !testClass.after().isEmpty()
                || (testClass.superclass().isPresent() && hasAfter(testClass.superclass().get()));
    }

    private static boolean hasBeforeClass(UnitTestClass testClass) {
        return !testClass.beforeClass().isEmpty()
                || (testClass.superclass().isPresent() && hasBeforeClass(
                        testClass.superclass().get()));
    }

    private static boolean hasAfterClass(UnitTestClass testClass) {
        return !testClass.afterClass().isEmpty()
                || (testClass.superclass().isPresent() && hasAfterClass(
                        testClass.superclass().get()));
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

    private static List<Member<UnitTest>> extractTests(UnitTestClass testClass) {
        List<Member<UnitTest>> tests = extractMembers(testClass, t -> t.tests().stream(), true)
                .collect(Collectors.toUnmodifiableList());
        return deduplicate(tests, UnitTest::name, false);
    }

    private static List<Member<FixtureMethod>> extractBefore(UnitTestClass testClass) {
        List<Member<FixtureMethod>> tests = extractMembers(testClass, t -> t.before().stream(), true)
                .collect(Collectors.toUnmodifiableList());
        return deduplicate(tests, FixtureMethod::name, false);
    }

    private static List<Member<FixtureMethod>> extractAfter(UnitTestClass testClass) {
        List<Member<FixtureMethod>> tests = extractMembers(testClass, t -> t.after().stream(), false)
                .collect(Collectors.toUnmodifiableList());
        return deduplicate(tests, FixtureMethod::name, true);
    }

    private static List<Member<FixtureMethod>> extractBeforeClass(UnitTestClass testClass) {
        return extractMembers(testClass, t -> t.beforeClass().stream(), true)
                        .collect(Collectors.toUnmodifiableList());
    }

    private static List<Member<FixtureMethod>> extractAfterClass(UnitTestClass testClass) {
        return extractMembers(testClass, t -> t.afterClass().stream(), false)
                        .collect(Collectors.toUnmodifiableList());
    }

    private static List<Member<TestRule>> extractInstanceRules(UnitTestClass testClass) {
        Stream<Member<TestRule>> ruleFields =
                extractMembers(testClass, t -> t.ruleFields().stream(), false);
        List<Member<TestRule>> ruleMethods =
                extractMembers(testClass, t -> t.ruleMethods().stream(), false)
                        .collect(Collectors.toUnmodifiableList());
        return Stream.concat(ruleFields, deduplicate(ruleMethods, TestRule::name, true).stream())
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Member<TestRule>> extractClassRules(UnitTestClass testClass) {
        Stream<Member<TestRule>> ruleFields =
                extractMembers(testClass, t -> t.classRuleFields().stream(), false);
        Stream<Member<TestRule>> ruleMethods =
                extractMembers(testClass, t -> t.classRuleMethods().stream(), false);
        return Stream.concat(ruleFields, ruleMethods).collect(Collectors.toUnmodifiableList());
    }

    private static Set<String> hiddenFieldNames(List<Member<TestRule>> rules) {
        Set<String> seen = new HashSet<>();
        Set<String> hidden = new HashSet<>();
        for (Member<TestRule> rule : rules) {
            String name = rule.member.name();
            if (!seen.add(name)) {
                hidden.add(name);
            }
        }
        return hidden;
    }

    private static class CodeTemplate {
        private final String code;

        private CodeTemplate(String code) {
            this.code = code;
        }

        public CodeTemplate withValue(String key, String value) {
            return new CodeTemplate(code.replace("{" + key + "}", value));
        }

        public CodeTemplate withValue(String key, Class<?> type) {
            return withValue(key, type.getCanonicalName());
        }

        public CodeTemplate withValue(String key, Enum<?> constant) {
            return withValue(key,
                    constant.getDeclaringClass().getCanonicalName() + "." + constant.name());
        }

        public CodeTemplate withValue(String key, CodeTemplate template) {
            return withValue(key, template.toString());
        }

        @Override
        public String toString() {
            return code;
        }

        public ClassOrInterfaceDeclaration toClassDeclaration() {
            return StaticJavaParser.parseTypeDeclaration(code).asClassOrInterfaceDeclaration();
        }

        public static CodeTemplate fromLines(String... lines) {
            return new CodeTemplate(String.join("\n", lines));
        }
    }

    private static List<CodeTemplate> generateBenchmarks(
            UnitTestClass testClass, String payloadsClassName) {
        CodeTemplate runBenchmark;
        if (hasRules(testClass)) {
            runBenchmark = CodeTemplate.fromLines("payloads.{name}.evaluate();");
        } else {
            runBenchmark = CodeTemplate.fromLines("this.runBenchmark(payloads.{name});");
        }
        CodeTemplate benchmarkTemplate = CodeTemplate.fromLines(
                        "@{Benchmark}",
                        "public void benchmark_{name}({_Payloads} payloads) throws {Throwable} {",
                        "  {def.runBenchmark}",
                        "}")
                .withValue("Benchmark", Benchmark.class)
                .withValue("_Payloads", payloadsClassName)
                .withValue("Throwable", Throwable.class)
                .withValue("def.runBenchmark", runBenchmark);
        return extractTests(testClass).stream()
                .map(t -> benchmarkTemplate.withValue("name", t.member.name()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static CodeTemplate generateInstanceAction(UnitTestClass testClass) {
        CodeTemplate instanceAction = CodeTemplate.fromLines(
                "{payload}.accept({instance});");
        CodeTemplate fixtureMethodCall = CodeTemplate.fromLines("{instance}.{name}();");
        if (hasAfter(testClass)) {

            String afterAction = extractAfter(testClass)
                    .stream()
                    .map(a -> fixtureMethodCall.withValue("name", a.member.name()).toString())
                    .collect(Collectors.joining("\n"));
            instanceAction = CodeTemplate.fromLines(
                            "try {",
                            "  {def.instanceAction}",
                            "} finally {",
                            "  {def.afterAction}",
                            "}")
                    .withValue("def.instanceAction", instanceAction)
                    .withValue("def.afterAction", afterAction);
        }
        if (hasBefore(testClass)) {
            String beforeAction = extractBefore(testClass)
                    .stream()
                    .map(b -> fixtureMethodCall.withValue("name", b.member.name()).toString())
                    .collect(Collectors.joining("\n"));
            instanceAction = CodeTemplate.fromLines(
                            "{def.beforeAction}",
                            "{def.instanceAction}")
                    .withValue("def.beforeAction", beforeAction)
                    .withValue("def.instanceAction", instanceAction);
        }
        return instanceAction;
    }

    private static CodeTemplate generateClassAction(UnitTestClass testClass) {
        CodeTemplate classAction = CodeTemplate.fromLines("{def.instanceAction}");
        Function<String, String> className =
                cn -> cn.equals(testClass.name()) ? ClassNames.shortClassName(cn) : cn;
        Function<Member<FixtureMethod>, CodeTemplate> fixtureMethodCall =
                fm -> CodeTemplate.fromLines("{class}.{name}();")
                        .withValue("class", className.apply(fm.declaringClass))
                        .withValue("name", fm.member.name());
        if (hasAfterClass(testClass)) {
            String afterClassAction = extractAfterClass(testClass).stream()
                    .map(fixtureMethodCall)
                    .map(CodeTemplate::toString)
                    .collect(Collectors.joining("\n"));
            classAction = CodeTemplate.fromLines(
                            "try {",
                            "  {def.classAction}",
                            "} finally {",
                            "  {def.afterClassAction}",
                            "}")
                    .withValue("def.classAction", classAction)
                    .withValue("def.afterClassAction", afterClassAction);
        }
        if (hasBeforeClass(testClass)) {
            String beforeClassAction = extractBeforeClass(testClass).stream()
                    .map(fixtureMethodCall)
                    .map(CodeTemplate::toString)
                    .collect(Collectors.joining("\n"));
            classAction = CodeTemplate.fromLines(
                            "{def.afterClassAction}",
                            "{def.classAction}")
                    .withValue("def.afterClassAction", beforeClassAction)
                    .withValue("def.classAction", classAction);
        }
        return classAction;
    }

    private static CodeTemplate generateRunBenchmark(UnitTestClass testClass) {
        if (hasRules(testClass)) {
            return CodeTemplate.fromLines("");
        }
        return CodeTemplate.fromLines(
                        "private void runBenchmark(",
                        "    {ThrowingConsumer}<{_Test}> payload) throws {Throwable} {",
                        "  {def.classAction}",
                        "}")
                .withValue("ThrowingConsumer", ThrowingConsumer.class)
                .withValue("Throwable", Throwable.class)
                .withValue("def.classAction", generateClassAction(testClass))
                .withValue("def.instanceAction", CodeTemplate.fromLines(
                        "  this.instance = new {_Test}();",
                        "  {def.instanceAction}"))
                .withValue("def.instanceAction", generateInstanceAction(testClass))
                .withValue("_Test", ClassNames.shortClassName(testClass.name()))
                .withValue("instance", "this.instance")
                .withValue("payload", "payload");
    }

    private static CodeTemplate generateInstanceStatement(
            UnitTestClass testClass, String name, String benchmarkClassName) {
        if (!hasInstanceRules(testClass)) {
            return CodeTemplate.fromLines("");
        }
        return CodeTemplate.fromLines(
                        "private static class {name} extends {Statement} {",
                        "  private final {ThrowingConsumer}<{_Test}> payload;",
                        "  private final {_Benchmark} benchmark;",
                        "",
                        "  public {name}(",
                        "      {ThrowingConsumer}<{_Test}> payload,",
                        "      {_Benchmark} benchmark) {",
                        "    this.payload = payload;",
                        "    this.benchmark = benchmark;",
                        "  }",
                        "",
                        "  @{Override}",
                        "  public void evaluate() throws {Throwable} {",
                        "    {def.instanceAction}",
                        "  }",
                        "}")
                .withValue("name", name)
                .withValue("Statement", org.junit.runners.model.Statement.class)
                .withValue("ThrowingConsumer", ThrowingConsumer.class)
                .withValue("_Test", ClassNames.shortClassName(testClass.name()))
                .withValue("_Benchmark", benchmarkClassName)
                .withValue("Override", Override.class)
                .withValue("Throwable", Throwable.class)
                .withValue("def.instanceAction", generateInstanceAction(testClass))
                .withValue("instance", "this.benchmark.instance")
                .withValue("payload", "this.payload");
    }

    private static CodeTemplate generateClassStatementInstanceAction(
            UnitTestClass testClass, String instanceStatementName) {
        if (!hasInstanceRules(testClass)) {
            return generateInstanceAction(testClass)
                    .withValue("instance", "this.benchmark.instance")
                    .withValue("payload", "this.payload");
        } else {
            List<Member<TestRule>> instanceRules = extractInstanceRules(testClass);
            Set<String> duplicateRuleFields = hiddenFieldNames(instanceRules);
            Function<Member<TestRule>, CodeTemplate> getRule = r -> {
                switch (r.member.source()) {
                    case FIELD:
                        if (duplicateRuleFields.contains(r.member.name())
                                && !r.declaringClass.equals(testClass.name())) {
                            return CodeTemplate.fromLines(
                                            "(({superclass}) this.benchmark.instance).{name}")
                                    .withValue("superclass", r.declaringClass)
                                    .withValue("name", r.member.name());
                        } else {
                            return CodeTemplate.fromLines("this.benchmark.instance.{name}")
                                    .withValue("name", r.member.name());
                        }
                    case METHOD:
                        return CodeTemplate.fromLines("this.benchmark.instance.{name}()")
                                .withValue("name", r.member.name());
                    default:
                        throw new AssertionError("Source should be either FIELD or METHOD.");
                }
            };
            CodeTemplate instanceRuleApplication = CodeTemplate.fromLines(
                    "statement = this.applyRule({getRule}, statement);");
            return CodeTemplate.fromLines(
                            "{Statement} statement =",
                            "   new {_InstanceStatement}(this.payload, this.benchmark);",
                            "{def.instanceRulesApplication}",
                            "statement.evaluate();")
                    .withValue("Statement", org.junit.runners.model.Statement.class)
                    .withValue("_InstanceStatement", instanceStatementName)
                    .withValue(
                            "def.instanceRulesApplication",
                            instanceRules.stream()
                                    .map(getRule)
                                    .map(gr -> instanceRuleApplication.withValue("getRule", gr))
                                    .map(CodeTemplate::toString)
                                    .collect(Collectors.joining("\n")));
        }
    }

    private static CodeTemplate generateClassStatement(
            UnitTestClass testClass, String name, String benchmarkClassName,
            String instanceStatementName) {
        if (!hasRules(testClass)) {
            return CodeTemplate.fromLines("");
        }
        Function<String, String> className =
                cn -> cn.equals(testClass.name()) ? ClassNames.shortClassName(cn) : cn;
        Function<Member<TestRule>, CodeTemplate> getClassRule = r -> {
            switch (r.member.source()) {
                case FIELD:
                    return CodeTemplate.fromLines("{class}.{name}")
                            .withValue("class", className.apply(r.declaringClass))
                            .withValue("name", r.member.name());
                case METHOD:
                    return CodeTemplate.fromLines("{class}.{name}()")
                            .withValue("class", className.apply(r.declaringClass))
                            .withValue("name", r.member.name());
                default:
                    throw new AssertionError("Source should be either FIELD or METHOD.");
            }
        };
        CodeTemplate applyClassRule = CodeTemplate.fromLines(
                "statement = {Rules}.apply({getClassRule}, statement, description);");
        boolean hasInstanceRules = hasInstanceRules(testClass);
        return CodeTemplate.fromLines(
                        "private static class {name} extends {Statement} {",
                        "  private final {ThrowingConsumer}<{_Test}> payload;",
                        "  private final {_Benchmark} benchmark;",
                        "  private final {Description} description;",
                        hasInstanceRules ? "  private final {FrameworkMethod} frameworkMethod;" : "",
                        "",
                        "  private {name}(",
                        "      {ThrowingConsumer}<{_Test}> payload,",
                        "      {_Benchmark} benchmark,",
                        "      {Description} description" + (hasInstanceRules ? "," : ") {"),
                        hasInstanceRules ? "      {FrameworkMethod} frameworkMethod) {" : "",
                        "    this.payload = payload;",
                        "    this.benchmark = benchmark;",
                        "    this.description = description;",
                        hasInstanceRules ? "    this.frameworkMethod = frameworkMethod;" : "",
                        "  }",
                        "",
                        "  @{Override}",
                        "  public void evaluate() throws {Throwable} {",
                        "    {def.classAction}",
                        "  }",
                        "",
                        hasInstanceRules ? "  private {Statement} applyRule(" : "",
                        hasInstanceRules ? "      {TestRule} rule, {Statement} statement) {" : "",
                        hasInstanceRules ? "    return {Rules}.apply(" : "",
                        hasInstanceRules ? "      rule, statement, this.description);" : "",
                        hasInstanceRules ? "  }" : "",
                        "",
                        hasInstanceRules ? "  private {Statement} applyRule(" : "",
                        hasInstanceRules ? "      {MethodRule} rule, {Statement} statement) {" : "",
                        hasInstanceRules ? "    return {Rules}.apply(" : "",
                        hasInstanceRules ? "      rule, statement, this.frameworkMethod," : "",
                        hasInstanceRules ? "      this.benchmark.instance);" : "",
                        hasInstanceRules ? "  }" : "",
                        "",
                        "  public static {Statement} forPayload(",
                        "      {ThrowingConsumer}<{_Test}> payload,",
                        "      String name,",
                        "      {_Benchmark} benchmark) {",
                        "    {Description} description = {Rules}.description({_Test}.class, name);",
                        hasInstanceRules ? "    {FrameworkMethod} frameworkMethod =" : "",
                        hasInstanceRules ? "      {Rules}.frameworkMethod({_Test}.class, name);" : "",
                        "    {Statement} statement = new {name}(",
                        "      payload,",
                        "      benchmark,",
                        "      description" + (hasInstanceRules ? "," : ");"),
                        hasInstanceRules ? "      frameworkMethod);" : "",
                        "    {def.classRulesApplication}",
                        "    return statement;",
                        "  }",
                        "}")
                .withValue("name", name)
                .withValue("Statement", org.junit.runners.model.Statement.class)
                .withValue("ThrowingConsumer", ThrowingConsumer.class)
                .withValue("_Benchmark", benchmarkClassName)
                .withValue("Description", Description.class)
                .withValue("FrameworkMethod", FrameworkMethod.class)
                .withValue("Override", Override.class)
                .withValue("Throwable", Throwable.class)
                .withValue("TestRule", org.junit.rules.TestRule.class)
                .withValue("MethodRule", MethodRule.class)
                .withValue("def.classAction", generateClassAction(testClass))
                .withValue("def.instanceAction", CodeTemplate.fromLines(
                        "    this.benchmark.instance = new {_Test}();",
                        "    {def.instanceAction}"))
                .withValue(
                        "def.instanceAction",
                        generateClassStatementInstanceAction(testClass, instanceStatementName))
                .withValue(
                        "def.classRulesApplication",
                        extractClassRules(testClass)
                                .stream()
                                .map(getClassRule)
                                .map(gcr -> applyClassRule.withValue("getClassRule", gcr))
                                .map(CodeTemplate::toString)
                                .collect(Collectors.joining("\n")))
                .withValue("_Test", ClassNames.shortClassName(testClass.name()))
                .withValue("Rules", Rules.class);
    }

    private static CodeTemplate generatePayloads(UnitTestClass testClass, String name) {
        CodeTemplate payload;
        if (hasRules(testClass)) {
            payload = CodeTemplate.fromLines("public {Statement} {name};")
                    .withValue("Statement", org.junit.runners.model.Statement.class);
        } else {
            payload = CodeTemplate.fromLines("public {ThrowingConsumer}<{_Test}> {name};")
                    .withValue("ThrowingConsumer", ThrowingConsumer.class)
                    .withValue("_Test", ClassNames.shortClassName(testClass.name()));
        }
        return CodeTemplate.fromLines(
                        "@{State}({Benchmark})",
                        "public static class {name} {",
                        "  {def.payloads}",
                        "}")
                .withValue("State", State.class)
                .withValue("Benchmark", Scope.Benchmark)
                .withValue("name", name)
                .withValue(
                        "def.payloads",
                        extractTests(testClass).stream()
                                .map(t -> payload.withValue("name", t.member.name()))
                                .map(CodeTemplate::toString)
                                .collect(Collectors.joining("\n")));
    }

    private static CodeTemplate generateMakePayloads(
            UnitTestClass testClass, String payloadsName, String classStatementName) {
        Function<UnitTest, CodeTemplate> getPayload = t -> {
            CodeTemplate payload = CodeTemplate.fromLines("{_Test}::{name}")
                    .withValue("_Test", ClassNames.shortClassName(testClass.name()))
                    .withValue("name", t.name());
            if (t.expectedException().isPresent()) {
                return CodeTemplate.fromLines(
                                "new {ExceptionTest}<>({def.getPayload}, {expected}.class)")
                        .withValue("ExceptionTest", ExceptionTest.class)
                        .withValue("def.getPayload", payload)
                        .withValue("expected", t.expectedException().get());
            } else {
                return payload;
            }
        };
        CodeTemplate makePayload;
        if (hasRules(testClass)) {
            makePayload = CodeTemplate.fromLines(
                            "payloads.{name} =",
                            "  {_ClassStatement}.forPayload({def.getPayload}, \"{name}\", this);")
                    .withValue("_ClassStatement", classStatementName);
        } else {
            makePayload = CodeTemplate.fromLines("payloads.{name} = {def.getPayload};");
        }
        return CodeTemplate.fromLines(
                        "@{Setup}({Trial})",
                        "public void makePayloads({payloadsClass} payloads) {",
                        "  {def.makePayloads}",
                        "}")
                .withValue("Setup", Setup.class)
                .withValue("Trial", Level.Trial)
                .withValue("payloadsClass", payloadsName)
                .withValue(
                        "def.makePayloads",
                        extractTests(testClass).stream()
                                .map(t -> makePayload.withValue("name", t.member.name())
                                        .withValue("def.getPayload", getPayload.apply(t.member)))
                                .map(CodeTemplate::toString)
                                .collect(Collectors.joining("\n")));
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
        String benchmarkClassName = getValidName("_Benchmark", nameValidator);
        String instanceStatementName = getValidName("_InstanceStatement", nameValidator);
        String classStatementName = getValidName("_ClassStatement", nameValidator);
        String payloadsName = getValidName("_Payloads", nameValidator);
        CodeTemplate benchmarkClass = CodeTemplate.fromLines(
                "@{State}({Thread})",
                "public static class {_Benchmark} {",
                "  private {_Test} instance;",
                "",
                "  {def.benchmarks}",
                "",
                "  {def.runBenchmark}",
                "",
                "  {def._InstanceStatement}",
                "",
                "  {def._ClassStatement}",
                "",
                "  {def._Payloads}",
                "",
                "  {def.makePayloads}",
                "}");
        benchmarkClass = benchmarkClass.withValue("State", State.class)
                .withValue("Thread", Scope.Thread)
                .withValue("_Benchmark", benchmarkClassName)
                .withValue("_Test", ClassNames.shortClassName(testClass.name()));
        benchmarkClass = benchmarkClass.withValue(
                        "def.benchmarks", String.join(
                                "\n\n",
                                generateBenchmarks(testClass, payloadsName)
                                        .stream()
                                        .map(CodeTemplate::toString)
                                        .toArray(String[]::new)))
                .withValue("def.runBenchmark", generateRunBenchmark(testClass))
                .withValue(
                        "def._InstanceStatement",
                        generateInstanceStatement(
                                testClass, instanceStatementName, benchmarkClassName))
                .withValue(
                        "def._ClassStatement",
                        generateClassStatement(
                                testClass, classStatementName, benchmarkClassName,
                                instanceStatementName))
                .withValue("def._Payloads", generatePayloads(testClass, payloadsName))
                .withValue(
                        "def.makePayloads",
                        generateMakePayloads(testClass, payloadsName, classStatementName));
        return benchmarkClass.toClassDeclaration();
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
}
