package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.junit.jupiter.api.Test;
import se.chalmers.ju2jmh.model.UnitTestClass;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.chalmers.ju2jmh.AstMatcher.equalsAst;

public class TailoredBenchmarkFactoryTest {
    private static CompilationUnit compilationUnitFromLines(String... lines) {
        return StaticJavaParser.parse(String.join("\n", lines));
    }

    private static ClassOrInterfaceDeclaration classFromLines(String... lines) {
        return StaticJavaParser.parseTypeDeclaration(String.join("\n", lines))
                .asClassOrInterfaceDeclaration();
    }

    private static MethodDeclaration methodFromLines(String... lines) {
        return StaticJavaParser.parseMethodDeclaration(String.join("\n", lines));
    }

    private static BlockStmt blockFromLines(String... lines) {
        return StaticJavaParser.parseBlock(String.join("\n", lines));
    }

    private static MethodDeclaration getMethod(
            ClassOrInterfaceDeclaration declaringClass, String name) {
        return declaringClass.getMethodsByName(name).get(0);
    }

    private static BlockStmt getMethodBody(
            ClassOrInterfaceDeclaration declaringClass, String name) {
        return getMethod(declaringClass, name).getBody().orElseThrow();
    }

    private static ClassOrInterfaceDeclaration getNestedClass(
            ClassOrInterfaceDeclaration parent, String name) {
        return parent.getMembers()
                .stream()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                .filter(c -> c.getNameAsString().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    public void handlesSimpleTest() {
        ClassOrInterfaceDeclaration expected = classFromLines(
                "@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)",
                "public static class _Benchmark {",
                "  private _Payloads payloads;",
                "  private SimpleTest instance;",
                "",
                "  @org.openjdk.jmh.annotations.Benchmark",
                "  public void benchmark_test() throws java.lang.Throwable {",
                "    this.runBenchmark(this.payloads.test);",
                "  }",
                "",
                "  private void runBenchmark(",
                "      se.chalmers.ju2jmh.api.ThrowingConsumer<SimpleTest> payload)",
                "        throws java.lang.Throwable {",
                "    this.instance = new SimpleTest();",
                "    payload.accept(this.instance);",
                "  }",
                "",
                "  private static class _Payloads {",
                "    public se.chalmers.ju2jmh.api.ThrowingConsumer<SimpleTest> test;",
                "  }",
                "",
                "  @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "  public void makePayloads() {",
                "    this.payloads = new _Payloads();",
                "    this.payloads.test = SimpleTest::test;",
                "  }",
                "}"
        );
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.SimpleTest")
                .withTest("test")
                .build();
        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);
        assertThat(benchmark, equalsAst(expected));
    }

    @Test
    public void handlesClassRule() {
        ClassOrInterfaceDeclaration expected = classFromLines(
                "@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)",
                "public static class _Benchmark {",
                "  private _Payloads payloads;",
                "  private ClassRuleTest instance;",
                "",
                "  @org.openjdk.jmh.annotations.Benchmark",
                "  public void benchmark_test() throws java.lang.Throwable {",
                "    this.payloads.test.evaluate();",
                "  }",
                "",
                "  private static class _ClassStatement",
                "      extends org.junit.runners.model.Statement {",
                "    private final se.chalmers.ju2jmh.api.ThrowingConsumer<ClassRuleTest> payload;",
                "    private final _Benchmark benchmark;",
                "",
                "    private _ClassStatement(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<ClassRuleTest> payload,",
                "        _Benchmark benchmark) {",
                "      this.payload = payload;",
                "      this.benchmark = benchmark;",
                "    }",
                "",
                "    @java.lang.Override",
                "    public void evaluate() throws java.lang.Throwable {",
                "      this.benchmark.instance = new ClassRuleTest();",
                "      this.payload.accept(this.benchmark.instance);",
                "    }",
                "",
                "",
                "    private static class _ApplyClassRulesStatement",
                "        extends org.junit.runners.model.Statement {",
                "      private final org.junit.runners.model.Statement statement;",
                "      private final org.junit.runner.Description description;",
                "",
                "      public _ApplyClassRulesStatement(",
                "          org.junit.runners.model.Statement statement,",
                "          org.junit.runner.Description description) {",
                "        this.statement = statement;",
                "        this.description = description;",
                "      }",
                "",
                "      @java.lang.Override",
                "      public void evaluate() throws java.lang.Throwable {",
                "        org.junit.runners.model.Statement statement = this.statement;",
                "        statement = se.chalmers.ju2jmh.api.Rules.apply(",
                "          ClassRuleTest.rule, statement, this.description);",
                "        statement.evaluate();",
                "      }",
                "    }",
                "",
                "    public static org.junit.runners.model.Statement forPayload(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<ClassRuleTest> payload,",
                "        String name,",
                "        _Benchmark benchmark) {",
                "      org.junit.runner.Description description =",
                "        se.chalmers.ju2jmh.api.Rules.description(ClassRuleTest.class, name);",
                "      org.junit.runners.model.Statement statement =",
                "        new _ClassStatement(payload, benchmark);",
                "      statement = new _ApplyClassRulesStatement(statement, description);",
                "      return statement;",
                "    }",
                "  }",
                "",
                "  private static class _Payloads {",
                "    public org.junit.runners.model.Statement test;",
                "  }",
                "",
                "  @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "  public void makePayloads() {",
                "    this.payloads = new _Payloads();",
                "    this.payloads.test =",
                "      _ClassStatement.forPayload(ClassRuleTest::test, \"test\", this);",
                "  }",
                "}"
        );
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.ClassRuleTest")
                .withTest("test")
                .withClassRuleField("rule")
                .build();
        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);
        assertThat(benchmark, equalsAst(expected));
    }

    @Test
    public void handlesInstanceRule() {
        ClassOrInterfaceDeclaration expected = classFromLines(
                "@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)",
                "public static class _Benchmark {",
                "  private _Payloads payloads;",
                "  private InstanceRuleTest instance;",
                "",
                "  @org.openjdk.jmh.annotations.Benchmark",
                "  public void benchmark_test() throws java.lang.Throwable {",
                "    this.payloads.test.evaluate();",
                "  }",
                "",
                "  private static class _InstanceStatement",
                "      extends org.junit.runners.model.Statement {",
                "    private final se.chalmers.ju2jmh.api.ThrowingConsumer<InstanceRuleTest>",
                "      payload;",
                "    private final _Benchmark benchmark;",
                "",
                "    public _InstanceStatement(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<InstanceRuleTest> payload,",
                "        _Benchmark benchmark) {",
                "      this.payload = payload;",
                "      this.benchmark = benchmark;",
                "    }",
                "",
                "    @java.lang.Override",
                "    public void evaluate() throws java.lang.Throwable {",
                "      this.payload.accept(this.benchmark.instance);",
                "    }",
                "  }",
                "",
                "  private static class _ClassStatement",
                "      extends org.junit.runners.model.Statement {",
                "    private final se.chalmers.ju2jmh.api.ThrowingConsumer<InstanceRuleTest>",
                "      payload;",
                "    private final _Benchmark benchmark;",
                "    private final org.junit.runner.Description description;",
                "    private final org.junit.runners.model.FrameworkMethod frameworkMethod;",
                "",
                "    private _ClassStatement(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<InstanceRuleTest> payload,",
                "        _Benchmark benchmark,",
                "        org.junit.runner.Description description,",
                "        org.junit.runners.model.FrameworkMethod frameworkMethod) {",
                "      this.payload = payload;",
                "      this.benchmark = benchmark;",
                "      this.description = description;",
                "      this.frameworkMethod = frameworkMethod;",
                "    }",
                "",
                "    @java.lang.Override",
                "    public void evaluate() throws java.lang.Throwable {",
                "      this.benchmark.instance = new InstanceRuleTest();",
                "      org.junit.runners.model.Statement statement =",
                "        new _InstanceStatement(this.payload, this.benchmark);",
                "      statement = this.applyRule(this.benchmark.instance.rule, statement);",
                "      statement.evaluate();",
                "    }",
                "",
                "    private org.junit.runners.model.Statement applyRule(",
                "        org.junit.rules.TestRule rule,",
                "        org.junit.runners.model.Statement statement) {",
                "      return se.chalmers.ju2jmh.api.Rules.apply(",
                "        rule, statement, this.description);",
                "    }",
                "",
                "    private org.junit.runners.model.Statement applyRule(",
                "        org.junit.rules.MethodRule rule,",
                "        org.junit.runners.model.Statement statement) {",
                "      return se.chalmers.ju2jmh.api.Rules.apply(",
                "        rule, statement, this.frameworkMethod, this.benchmark.instance);",
                "    }",
                "",
                "    public static org.junit.runners.model.Statement forPayload(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<InstanceRuleTest> payload,",
                "        String name,",
                "        _Benchmark benchmark) {",
                "      org.junit.runner.Description description =",
                "        se.chalmers.ju2jmh.api.Rules.description(InstanceRuleTest.class, name);",
                "      org.junit.runners.model.FrameworkMethod frameworkMethod =",
                "        se.chalmers.ju2jmh.api.Rules.frameworkMethod(",
                "          InstanceRuleTest.class, name);",
                "      org.junit.runners.model.Statement statement =",
                "        new _ClassStatement(payload, benchmark, description, frameworkMethod);",
                "      return statement;",
                "    }",
                "  }",
                "",
                "  private static class _Payloads {",
                "    public org.junit.runners.model.Statement test;",
                "  }",
                "",
                "  @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "  public void makePayloads() {",
                "    this.payloads = new _Payloads();",
                "    this.payloads.test =",
                "      _ClassStatement.forPayload(InstanceRuleTest::test, \"test\", this);",
                "  }",
                "}"
        );
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.InstanceRuleTest")
                .withTest("test")
                .withInstanceRuleField("rule")
                .build();
        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);
        assertThat(benchmark, equalsAst(expected));
    }

    @Test
    public void handlesClassAndInstanceRules() {
        ClassOrInterfaceDeclaration expected = classFromLines(
                "@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)",
                "public static class _Benchmark {",
                "  private _Payloads payloads;",
                "  private Test instance;",
                "",
                "  @org.openjdk.jmh.annotations.Benchmark",
                "  public void benchmark_test() throws java.lang.Throwable {",
                "    this.payloads.test.evaluate();",
                "  }",
                "",
                "  private static class _InstanceStatement",
                "      extends org.junit.runners.model.Statement {",
                "    private final se.chalmers.ju2jmh.api.ThrowingConsumer<Test> payload;",
                "    private final _Benchmark benchmark;",
                "",
                "    public _InstanceStatement(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<Test> payload,",
                "        _Benchmark benchmark) {",
                "      this.payload = payload;",
                "      this.benchmark = benchmark;",
                "    }",
                "",
                "    @java.lang.Override",
                "    public void evaluate() throws java.lang.Throwable {",
                "      this.payload.accept(this.benchmark.instance);",
                "    }",
                "  }",
                "",
                "  private static class _ClassStatement",
                "      extends org.junit.runners.model.Statement {",
                "    private final se.chalmers.ju2jmh.api.ThrowingConsumer<Test> payload;",
                "    private final _Benchmark benchmark;",
                "    private final org.junit.runner.Description description;",
                "    private final org.junit.runners.model.FrameworkMethod frameworkMethod;",
                "",
                "    private _ClassStatement(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<Test> payload,",
                "        _Benchmark benchmark,",
                "        org.junit.runner.Description description,",
                "        org.junit.runners.model.FrameworkMethod frameworkMethod) {",
                "      this.payload = payload;",
                "      this.benchmark = benchmark;",
                "      this.description = description;",
                "      this.frameworkMethod = frameworkMethod;",
                "    }",
                "",
                "    @java.lang.Override",
                "    public void evaluate() throws java.lang.Throwable {",
                "      this.benchmark.instance = new Test();",
                "      org.junit.runners.model.Statement statement =",
                "        new _InstanceStatement(this.payload, this.benchmark);",
                "      statement = this.applyRule(this.benchmark.instance.rule, statement);",
                "      statement.evaluate();",
                "    }",
                "",
                "    private org.junit.runners.model.Statement applyRule(",
                "        org.junit.rules.TestRule rule,",
                "        org.junit.runners.model.Statement statement) {",
                "      return se.chalmers.ju2jmh.api.Rules.apply(",
                "        rule, statement, this.description);",
                "    }",
                "",
                "    private org.junit.runners.model.Statement applyRule(",
                "        org.junit.rules.MethodRule rule,",
                "        org.junit.runners.model.Statement statement) {",
                "      return se.chalmers.ju2jmh.api.Rules.apply(",
                "        rule, statement, this.frameworkMethod, this.benchmark.instance);",
                "    }",
                "",
                "    private static class _ApplyClassRulesStatement",
                "        extends org.junit.runners.model.Statement {",
                "      private final org.junit.runners.model.Statement statement;",
                "      private final org.junit.runner.Description description;",
                "",
                "      public _ApplyClassRulesStatement(",
                "          org.junit.runners.model.Statement statement,",
                "          org.junit.runner.Description description) {",
                "        this.statement = statement;",
                "        this.description = description;",
                "      }",
                "",
                "      @java.lang.Override",
                "      public void evaluate() throws java.lang.Throwable {",
                "        org.junit.runners.model.Statement statement = this.statement;",
                "        statement = se.chalmers.ju2jmh.api.Rules.apply(",
                "          Test.classRule, statement, this.description);",
                "        statement.evaluate();",
                "      }",
                "    }",
                "",
                "    public static org.junit.runners.model.Statement forPayload(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<Test> payload,",
                "        String name,",
                "        _Benchmark benchmark) {",
                "      org.junit.runner.Description description =",
                "        se.chalmers.ju2jmh.api.Rules.description(Test.class, name);",
                "      org.junit.runners.model.FrameworkMethod frameworkMethod =",
                "        se.chalmers.ju2jmh.api.Rules.frameworkMethod(Test.class, name);",
                "      org.junit.runners.model.Statement statement =",
                "        new _ClassStatement(payload, benchmark, description, frameworkMethod);",
                "      statement = new _ApplyClassRulesStatement(statement, description);",
                "      return statement;",
                "    }",
                "  }",
                "",
                "  private static class _Payloads {",
                "    public org.junit.runners.model.Statement test;",
                "  }",
                "",
                "  @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "  public void makePayloads() {",
                "    this.payloads = new _Payloads();",
                "    this.payloads.test =",
                "      _ClassStatement.forPayload(Test::test, \"test\", this);",
                "  }",
                "}"
        );
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withTest("test")
                .withInstanceRuleField("rule")
                .withClassRuleField("classRule")
                .build();
        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);
        assertThat(benchmark, equalsAst(expected));
    }

    @Test
    public void handlesMultipleTestsWithoutRules() {
        MethodDeclaration test1Expected = methodFromLines(
                "@org.openjdk.jmh.annotations.Benchmark",
                "public void benchmark_test1() throws java.lang.Throwable {",
                "  this.runBenchmark(this.payloads.test1);",
                "}");
        MethodDeclaration test2Expected = methodFromLines(
                "@org.openjdk.jmh.annotations.Benchmark",
                "public void benchmark_test2() throws java.lang.Throwable {",
                "  this.runBenchmark(this.payloads.test2);",
                "}");
        ClassOrInterfaceDeclaration payloadsExpected = classFromLines(
                "private static class _Payloads {",
                "  public se.chalmers.ju2jmh.api.ThrowingConsumer<Test> test1;",
                "  public se.chalmers.ju2jmh.api.ThrowingConsumer<Test> test2;",
                "}");
        MethodDeclaration makePayloadsExpected = methodFromLines(
                "@org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "public void makePayloads() {",
                "  this.payloads = new _Payloads();",
                "  this.payloads.test1 = Test::test1;",
                "  this.payloads.test2 = Test::test2;",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withTest("test1")
                .withTest("test2")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        MethodDeclaration test1 = getMethod(benchmark, "benchmark_test1");
        MethodDeclaration test2 = getMethod(benchmark, "benchmark_test2");
        ClassOrInterfaceDeclaration payloads = getNestedClass(benchmark, "_Payloads");
        MethodDeclaration makePayloads = getMethod(benchmark, "makePayloads");
        assertThat(test1, equalsAst(test1Expected));
        assertThat(test2, equalsAst(test2Expected));
        assertThat(payloads, equalsAst(payloadsExpected));
        assertThat(makePayloads, equalsAst(makePayloadsExpected));
    }

    @Test
    public void handlesMultipleTestsWithRules() {
        MethodDeclaration test1Expected = methodFromLines(
                "@org.openjdk.jmh.annotations.Benchmark",
                "public void benchmark_test1() throws java.lang.Throwable {",
                "  this.payloads.test1.evaluate();",
                "}");
        MethodDeclaration test2Expected = methodFromLines(
                "@org.openjdk.jmh.annotations.Benchmark",
                "public void benchmark_test2() throws java.lang.Throwable {",
                "  this.payloads.test2.evaluate();",
                "}");
        ClassOrInterfaceDeclaration payloadsExpected = classFromLines(
                "private static class _Payloads {",
                "  public org.junit.runners.model.Statement test1;",
                "  public org.junit.runners.model.Statement test2;",
                "}");
        MethodDeclaration makePayloadsExpected = methodFromLines(
                "@org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "public void makePayloads() {",
                "  this.payloads = new _Payloads();",
                "  this.payloads.test1 = _ClassStatement.forPayload(Test::test1, \"test1\", this);",
                "  this.payloads.test2 = _ClassStatement.forPayload(Test::test2, \"test2\", this);",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withTest("test1")
                .withTest("test2")
                .withClassRuleField("rule")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        MethodDeclaration test1 = getMethod(benchmark, "benchmark_test1");
        MethodDeclaration test2 = getMethod(benchmark, "benchmark_test2");
        ClassOrInterfaceDeclaration payloads = getNestedClass(benchmark, "_Payloads");
        MethodDeclaration makePayloads = getMethod(benchmark, "makePayloads");
        assertThat(test1, equalsAst(test1Expected));
        assertThat(test2, equalsAst(test2Expected));
        assertThat(payloads, equalsAst(payloadsExpected));
        assertThat(makePayloads, equalsAst(makePayloadsExpected));
    }

    @Test
    public void handlesExceptionTestsWithoutRules() {
        MethodDeclaration makePayloadsExpected = methodFromLines(
                "@org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "public void makePayloads() {",
                "  this.payloads = new _Payloads();",
                "  this.payloads.exceptionTest = new se.chalmers.ju2jmh.api.ExceptionTest<>(",
                "    Test::exceptionTest, java.lang.Exception.class);",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withExceptionTest("exceptionTest", "java.lang.Exception")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        MethodDeclaration makePayloads = getMethod(benchmark, "makePayloads");
        assertThat(makePayloads, equalsAst(makePayloadsExpected));
    }

    @Test
    public void handlesExceptionTestsWithRules() {
        MethodDeclaration makePayloadsExpected = methodFromLines(
                "@org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "public void makePayloads() {",
                "  this.payloads = new _Payloads();",
                "  this.payloads.exceptionTest = _ClassStatement.forPayload(",
                "    new se.chalmers.ju2jmh.api.ExceptionTest<>(",
                "      Test::exceptionTest, java.lang.Exception.class),",
                "    \"exceptionTest\", this);",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withExceptionTest("exceptionTest", "java.lang.Exception")
                .withClassRuleField("rule")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        MethodDeclaration makePayloads = getMethod(benchmark, "makePayloads");
        assertThat(makePayloads, equalsAst(makePayloadsExpected));
    }

    @Test
    public void handlesBeforeWithoutRules() {
        BlockStmt expectedRunBenchmarkBody = blockFromLines(
                "{",
                "  this.instance = new Test();",
                "  this.instance.before1();",
                "  this.instance.before2();",
                "  payload.accept(this.instance);",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withBefore("before1")
                .withBefore("before2")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt runBenchmarkBody = getMethodBody(benchmark, "runBenchmark");
        assertThat(runBenchmarkBody, equalsAst(expectedRunBenchmarkBody));
    }

    @Test
    public void handlesAfterWithoutRules() {
        BlockStmt expectedRunBenchmarkBody = blockFromLines(
                "{",
                "  this.instance = new Test();",
                "  try {",
                "    payload.accept(this.instance);",
                "  } finally {",
                "    this.instance.after1();",
                "    this.instance.after2();",
                "  }",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withAfter("after1")
                .withAfter("after2")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt runBenchmarkBody = getMethodBody(benchmark, "runBenchmark");
        assertThat(runBenchmarkBody, equalsAst(expectedRunBenchmarkBody));
    }

    @Test
    public void handlesBeforeClassWithoutRules() {
        BlockStmt expectedRunBenchmarkBody = blockFromLines(
                "{",
                "  Test.beforeClass1();",
                "  Test.beforeClass2();",
                "  this.instance = new Test();",
                "  payload.accept(this.instance);",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withBeforeClass("beforeClass1")
                .withBeforeClass("beforeClass2")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt runBenchmarkBody = getMethodBody(benchmark, "runBenchmark");
        assertThat(runBenchmarkBody, equalsAst(expectedRunBenchmarkBody));
    }

    @Test
    public void handlesAfterClassWithoutRules() {
        BlockStmt expectedRunBenchmarkBody = blockFromLines(
                "{",
                "  try {",
                "    this.instance = new Test();",
                "    payload.accept(this.instance);",
                "  } finally {",
                "    Test.afterClass1();",
                "    Test.afterClass2();",
                "  }",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withTest("test")
                .withAfterClass("afterClass1")
                .withAfterClass("afterClass2")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt runBenchmarkBody = getMethodBody(benchmark, "runBenchmark");
        assertThat(runBenchmarkBody, equalsAst(expectedRunBenchmarkBody));
    }

    @Test
    public void handlesMultipleFixtureMethodsWithoutRules() {
        BlockStmt expectedRunBenchmarkBody = blockFromLines(
                "{",
                "  Test.beforeClass();",
                "  try {",
                "    this.instance = new Test();",
                "    this.instance.before();",
                "    try {",
                "      payload.accept(this.instance);",
                "    } finally {",
                "      this.instance.after();",
                "    }",
                "  } finally {",
                "    Test.afterClass();",
                "  }",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withBefore("before")
                .withAfter("after")
                .withBeforeClass("beforeClass")
                .withAfterClass("afterClass")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt runBenchmarkBody = getMethodBody(benchmark, "runBenchmark");
        assertThat(runBenchmarkBody, equalsAst(expectedRunBenchmarkBody));
    }

    @Test
    public void handlesFixtureMethodsWithClassRules() {
        BlockStmt expectedEvaluateBody = blockFromLines(
                "{",
                "  Test.beforeClass();",
                "  try {",
                "    this.benchmark.instance = new Test();",
                "    this.benchmark.instance.before();",
                "    try {",
                "      this.payload.accept(this.benchmark.instance);",
                "    } finally {",
                "      this.benchmark.instance.after();",
                "    }",
                "  } finally {",
                "    Test.afterClass();",
                "  }",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withBefore("before")
                .withAfter("after")
                .withBeforeClass("beforeClass")
                .withAfterClass("afterClass")
                .withClassRuleField("rule")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt evaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        assertThat(evaluateBody, equalsAst(expectedEvaluateBody));
    }

    @Test
    public void handlesFixtureMethodsWithInstanceRules() {
        BlockStmt expectedInstanceEvaluateBody = blockFromLines(
                "{",
                "  this.benchmark.instance.before();",
                "  try {",
                "    this.payload.accept(this.benchmark.instance);",
                "  } finally {",
                "    this.benchmark.instance.after();",
                "  }",
                "}");
        BlockStmt expectedClassEvaluateBody = blockFromLines(
                "{",
                "  Test.beforeClass();",
                "  try {",
                "    this.benchmark.instance = new Test();",
                "    org.junit.runners.model.Statement statement =",
                "      new _InstanceStatement(this.payload, this.benchmark);",
                "    statement = this.applyRule(this.benchmark.instance.rule, statement);",
                "    statement.evaluate();",
                "  } finally {",
                "    Test.afterClass();",
                "  }",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withBefore("before")
                .withAfter("after")
                .withBeforeClass("beforeClass")
                .withAfterClass("afterClass")
                .withInstanceRuleField("rule")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt instanceEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_InstanceStatement"), "evaluate");
        BlockStmt classEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        assertThat(instanceEvaluateBody, equalsAst(expectedInstanceEvaluateBody));
        assertThat(classEvaluateBody, equalsAst(expectedClassEvaluateBody));
    }

    @Test
    public void handlesSuperclassFixtureMethodsWithoutRules() {
        BlockStmt expectedRunBenchmarkBody = blockFromLines(
                "{",
                "  com.example.TestSuperclass.beforeClass();",
                "  try {",
                "    this.instance = new Test();",
                "    this.instance.before();",
                "    try {",
                "      payload.accept(this.instance);",
                "    } finally {",
                "      this.instance.after();",
                "    }",
                "  } finally {",
                "    com.example.TestSuperclass.afterClass();",
                "  }",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withBefore("before")
                .withAfter("after")
                .withBeforeClass("beforeClass")
                .withAfterClass("afterClass")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt runBenchmarkBody = getMethodBody(benchmark, "runBenchmark");
        assertThat(runBenchmarkBody, equalsAst(expectedRunBenchmarkBody));
    }

    @Test
    public void handlesSuperclassFixtureMethodsWithClassRules() {
        BlockStmt expectedEvaluateBody = blockFromLines(
                "{",
                "  com.example.TestSuperclass.beforeClass();",
                "  try {",
                "    this.benchmark.instance = new Test();",
                "    this.benchmark.instance.before();",
                "    try {",
                "      this.payload.accept(this.benchmark.instance);",
                "    } finally {",
                "      this.benchmark.instance.after();",
                "    }",
                "  } finally {",
                "    com.example.TestSuperclass.afterClass();",
                "  }",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withBefore("before")
                .withAfter("after")
                .withBeforeClass("beforeClass")
                .withAfterClass("afterClass")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .withClassRuleField("rule")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt evaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        assertThat(evaluateBody, equalsAst(expectedEvaluateBody));
    }

    @Test
    public void handlesSuperclassFixtureMethodsWithInstanceRules() {
        BlockStmt expectedInstanceEvaluateBody = blockFromLines(
                "{",
                "  this.benchmark.instance.before();",
                "  try {",
                "    this.payload.accept(this.benchmark.instance);",
                "  } finally {",
                "    this.benchmark.instance.after();",
                "  }",
                "}");
        BlockStmt expectedClassEvaluateBody = blockFromLines(
                "{",
                "  com.example.TestSuperclass.beforeClass();",
                "  try {",
                "    this.benchmark.instance = new Test();",
                "    org.junit.runners.model.Statement statement =",
                "      new _InstanceStatement(this.payload, this.benchmark);",
                "    statement = this.applyRule(this.benchmark.instance.rule, statement);",
                "    statement.evaluate();",
                "  } finally {",
                "    com.example.TestSuperclass.afterClass();",
                "  }",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withBefore("before")
                .withAfter("after")
                .withBeforeClass("beforeClass")
                .withAfterClass("afterClass")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .withInstanceRuleField("rule")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt instanceEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_InstanceStatement"), "evaluate");
        BlockStmt classEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        assertThat(instanceEvaluateBody, equalsAst(expectedInstanceEvaluateBody));
        assertThat(classEvaluateBody, equalsAst(expectedClassEvaluateBody));
    }

    @Test
    public void handlesMultipleInstanceRules() {
        BlockStmt expectedClassEvaluateBody = blockFromLines(
                "{",
                "  this.benchmark.instance = new Test();",
                "  org.junit.runners.model.Statement statement =",
                "    new _InstanceStatement(this.payload, this.benchmark);",
                "  statement = this.applyRule(this.benchmark.instance.rule1, statement);",
                "  statement = this.applyRule(this.benchmark.instance.rule2, statement);",
                "  statement.evaluate();",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withInstanceRuleField("rule1")
                .withInstanceRuleField("rule2")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt classEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        assertThat(classEvaluateBody, equalsAst(expectedClassEvaluateBody));
    }

    @Test
    public void handlesMultipleClassRules() {
        BlockStmt expectedApplyRulesEvaluateBody = blockFromLines(
                "{",
                "  org.junit.runners.model.Statement statement = this.statement;",
                "  statement = se.chalmers.ju2jmh.api.Rules.apply(",
                "    Test.rule1, statement, this.description);",
                "  statement = se.chalmers.ju2jmh.api.Rules.apply(",
                "    Test.rule2, statement, this.description);",
                "  statement.evaluate();",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withClassRuleField("rule1")
                .withClassRuleField("rule2")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt applyRulesEvaluateBody = getMethodBody(
                getNestedClass(
                        getNestedClass(benchmark, "_ClassStatement"), "_ApplyClassRulesStatement"),
                "evaluate");
        assertThat(applyRulesEvaluateBody, equalsAst(expectedApplyRulesEvaluateBody));
    }

    @Test
    public void handlesRuleMethods() {
        BlockStmt expectedClassEvaluateBody = blockFromLines(
                "{",
                "  this.benchmark.instance = new Test();",
                "  org.junit.runners.model.Statement statement =",
                "    new _InstanceStatement(this.payload, this.benchmark);",
                "  statement = this.applyRule(this.benchmark.instance.rule(), statement);",
                "  statement.evaluate();",
                "}");
        BlockStmt expectedApplyRulesEvaluateBody = blockFromLines(
                "{",
                "  org.junit.runners.model.Statement statement = this.statement;",
                "  statement = se.chalmers.ju2jmh.api.Rules.apply(",
                "    Test.classRule(), statement, this.description);",
                "  statement.evaluate();",
                "}");
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withInstanceRuleMethod("rule")
                .withClassRuleMethod("classRule")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt classEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        BlockStmt applyRulesEvaluateBody = getMethodBody(
                getNestedClass(
                        getNestedClass(benchmark, "_ClassStatement"), "_ApplyClassRulesStatement"),
                "evaluate");
        assertThat(classEvaluateBody, equalsAst(expectedClassEvaluateBody));
        assertThat(applyRulesEvaluateBody, equalsAst(expectedApplyRulesEvaluateBody));
    }

    @Test
    public void handlesSuperclassRules() {
        BlockStmt expectedClassEvaluateBody = blockFromLines(
                "{",
                "  this.benchmark.instance = new Test();",
                "  org.junit.runners.model.Statement statement =",
                "    new _InstanceStatement(this.payload, this.benchmark);",
                "  statement = this.applyRule(this.benchmark.instance.ruleField, statement);",
                "  statement = this.applyRule(this.benchmark.instance.ruleMethod(), statement);",
                "  statement.evaluate();",
                "}");
        BlockStmt expectedApplyRulesEvaluateBody = blockFromLines(
                "{",
                "  org.junit.runners.model.Statement statement = this.statement;",
                "  statement = se.chalmers.ju2jmh.api.Rules.apply(",
                "    com.example.TestSuperclass.classRuleField, statement, this.description);",
                "  statement = se.chalmers.ju2jmh.api.Rules.apply(",
                "    com.example.TestSuperclass.classRuleMethod(), statement, this.description);",
                "  statement.evaluate();",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withInstanceRuleField("ruleField")
                .withInstanceRuleMethod("ruleMethod")
                .withClassRuleField("classRuleField")
                .withClassRuleMethod("classRuleMethod")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt classEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        BlockStmt applyRulesEvaluateBody = getMethodBody(
                getNestedClass(
                        getNestedClass(benchmark, "_ClassStatement"), "_ApplyClassRulesStatement"),
                "evaluate");
        assertThat(classEvaluateBody, equalsAst(expectedClassEvaluateBody));
        assertThat(applyRulesEvaluateBody, equalsAst(expectedApplyRulesEvaluateBody));
    }

    @Test
    public void handlesHiddenRuleFields() {
        BlockStmt expectedClassEvaluateBody = blockFromLines(
                "{",
                "  this.benchmark.instance = new Test();",
                "  org.junit.runners.model.Statement statement =",
                "    new _InstanceStatement(this.payload, this.benchmark);",
                "  statement = this.applyRule(this.benchmark.instance.rule, statement);",
                "  statement = this.applyRule(",
                "    ((com.example.TestSuperclass) this.benchmark.instance).rule, statement);",
                "  statement.evaluate();",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withInstanceRuleField("rule")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .withInstanceRuleField("rule")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt classEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        assertThat(classEvaluateBody, equalsAst(expectedClassEvaluateBody));
    }

    @Test
    public void handlesOverriddenTests() {
        BlockStmt expectedMakePayloadsBody = blockFromLines(
                "{",
                "  this.payloads = new _Payloads();",
                "  this.payloads.test1 = Test::test1;",
                "  this.payloads.test3 = Test::test3;",
                "  this.payloads.test4 = Test::test4;",
                "  this.payloads.test2 = Test::test2;",
                "  this.payloads.test5 = Test::test5;",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withTest("test1")
                .withTest("test2")
                .withTest("test3")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .withTest("test4")
                .withTest("test2")
                .withTest("test5")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        List<String> methodNames = benchmark.getMethods()
                .stream()
                .map(NodeWithSimpleName::getNameAsString)
                .collect(Collectors.toUnmodifiableList());
        BlockStmt makePayloadsBody = getMethodBody(benchmark, "makePayloads");
        assertIterableEquals(
                List.of("benchmark_test1", "benchmark_test3", "benchmark_test4", "benchmark_test2",
                        "benchmark_test5", "runBenchmark", "makePayloads"),
                methodNames);
        assertThat(makePayloadsBody, equalsAst(expectedMakePayloadsBody));
    }

    @Test
    public void handlesOverriddenBefore() {
        BlockStmt expectedRunBenchmarkBody = blockFromLines(
                "{",
                "  this.instance = new Test();",
                "  this.instance.before1();",
                "  this.instance.before3();",
                "  this.instance.before4();",
                "  this.instance.before2();",
                "  this.instance.before5();",
                "  payload.accept(this.instance);",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withBefore("before1")
                .withBefore("before2")
                .withBefore("before3")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .withBefore("before4")
                .withBefore("before2")
                .withBefore("before5")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt runBenchmarkBody = getMethodBody(benchmark, "runBenchmark");
        assertThat(runBenchmarkBody, equalsAst(expectedRunBenchmarkBody));
    }

    @Test
    public void handlesOverriddenAfter() {
        BlockStmt expectedRunBenchmarkBody = blockFromLines(
                "{",
                "  this.instance = new Test();",
                "  try {",
                "    payload.accept(this.instance);",
                "  } finally {",
                "    this.instance.after4();",
                "    this.instance.after2();",
                "    this.instance.after5();",
                "    this.instance.after1();",
                "    this.instance.after3();",
                "  }",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withAfter("after1")
                .withAfter("after2")
                .withAfter("after3")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .withAfter("after4")
                .withAfter("after2")
                .withAfter("after5")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt runBenchmarkBody = getMethodBody(benchmark, "runBenchmark");
        assertThat(runBenchmarkBody, equalsAst(expectedRunBenchmarkBody));
    }

    @Test
    public void handlesOverriddenRuleMethods() {
        BlockStmt expectedClassEvaluateBody = blockFromLines(
                "{",
                "  this.benchmark.instance = new Test();",
                "  org.junit.runners.model.Statement statement =",
                "    new _InstanceStatement(this.payload, this.benchmark);",
                "  statement = this.applyRule(this.benchmark.instance.rule4(), statement);",
                "  statement = this.applyRule(this.benchmark.instance.rule2(), statement);",
                "  statement = this.applyRule(this.benchmark.instance.rule5(), statement);",
                "  statement = this.applyRule(this.benchmark.instance.rule1(), statement);",
                "  statement = this.applyRule(this.benchmark.instance.rule3(), statement);",
                "  statement.evaluate();",
                "}");
        UnitTestClass testSuperclass = UnitTestClass.Builder.forClass("com.example.TestSuperclass")
                .withInstanceRuleMethod("rule1")
                .withInstanceRuleMethod("rule2")
                .withInstanceRuleMethod("rule3")
                .build();
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withSuperclass(testSuperclass)
                .withInstanceRuleMethod("rule4")
                .withInstanceRuleMethod("rule2")
                .withInstanceRuleMethod("rule5")
                .build();

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass);

        BlockStmt classEvaluateBody = getMethodBody(
                getNestedClass(benchmark, "_ClassStatement"), "evaluate");
        assertThat(classEvaluateBody, equalsAst(expectedClassEvaluateBody));
    }

    @Test
    public void handlesNameConflicts() {
        ClassOrInterfaceDeclaration expected = classFromLines(
                "@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)",
                "public static class _Benchmark_0 {",
                "  private _Payloads_1 payloads;",
                "  private Test instance;",
                "",
                "  @org.openjdk.jmh.annotations.Benchmark",
                "  public void benchmark_test() throws java.lang.Throwable {",
                "    this.payloads.test.evaluate();",
                "  }",
                "",
                "  private static class _InstanceStatement_2",
                "      extends org.junit.runners.model.Statement {",
                "    private final se.chalmers.ju2jmh.api.ThrowingConsumer<Test>",
                "      payload;",
                "    private final _Benchmark_0 benchmark;",
                "",
                "    public _InstanceStatement_2(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<Test> payload,",
                "        _Benchmark_0 benchmark) {",
                "      this.payload = payload;",
                "      this.benchmark = benchmark;",
                "    }",
                "",
                "    @java.lang.Override",
                "    public void evaluate() throws java.lang.Throwable {",
                "      this.payload.accept(this.benchmark.instance);",
                "    }",
                "  }",
                "",
                "  private static class _ClassStatement_3",
                "      extends org.junit.runners.model.Statement {",
                "    private final se.chalmers.ju2jmh.api.ThrowingConsumer<Test>",
                "      payload;",
                "    private final _Benchmark_0 benchmark;",
                "    private final org.junit.runner.Description description;",
                "    private final org.junit.runners.model.FrameworkMethod frameworkMethod;",
                "",
                "    private _ClassStatement_3(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<Test> payload,",
                "        _Benchmark_0 benchmark,",
                "        org.junit.runner.Description description,",
                "        org.junit.runners.model.FrameworkMethod frameworkMethod) {",
                "      this.payload = payload;",
                "      this.benchmark = benchmark;",
                "      this.description = description;",
                "      this.frameworkMethod = frameworkMethod;",
                "    }",
                "",
                "    @java.lang.Override",
                "    public void evaluate() throws java.lang.Throwable {",
                "      this.benchmark.instance = new Test();",
                "      org.junit.runners.model.Statement statement =",
                "        new _InstanceStatement_2(this.payload, this.benchmark);",
                "      statement = this.applyRule(this.benchmark.instance.rule, statement);",
                "      statement.evaluate();",
                "    }",
                "",
                "    private org.junit.runners.model.Statement applyRule(",
                "        org.junit.rules.TestRule rule,",
                "        org.junit.runners.model.Statement statement) {",
                "      return se.chalmers.ju2jmh.api.Rules.apply(",
                "        rule, statement, this.description);",
                "    }",
                "",
                "    private org.junit.runners.model.Statement applyRule(",
                "        org.junit.rules.MethodRule rule,",
                "        org.junit.runners.model.Statement statement) {",
                "      return se.chalmers.ju2jmh.api.Rules.apply(",
                "        rule, statement, this.frameworkMethod, this.benchmark.instance);",
                "    }",
                "",
                "    private static class _ApplyClassRulesStatement_4",
                "        extends org.junit.runners.model.Statement {",
                "      private final org.junit.runners.model.Statement statement;",
                "      private final org.junit.runner.Description description;",
                "",
                "      public _ApplyClassRulesStatement_4(",
                "          org.junit.runners.model.Statement statement,",
                "          org.junit.runner.Description description) {",
                "        this.statement = statement;",
                "        this.description = description;",
                "      }",
                "",
                "      @java.lang.Override",
                "      public void evaluate() throws java.lang.Throwable {",
                "        org.junit.runners.model.Statement statement = this.statement;",
                "        statement = se.chalmers.ju2jmh.api.Rules.apply(",
                "          Test.classRule, statement, this.description);",
                "        statement.evaluate();",
                "      }",
                "    }",
                "",
                "    public static org.junit.runners.model.Statement forPayload(",
                "        se.chalmers.ju2jmh.api.ThrowingConsumer<Test> payload,",
                "        String name,",
                "        _Benchmark_0 benchmark) {",
                "      org.junit.runner.Description description =",
                "        se.chalmers.ju2jmh.api.Rules.description(Test.class, name);",
                "      org.junit.runners.model.FrameworkMethod frameworkMethod =",
                "        se.chalmers.ju2jmh.api.Rules.frameworkMethod(Test.class, name);",
                "      org.junit.runners.model.Statement statement =",
                "        new _ClassStatement_3(payload, benchmark, description, frameworkMethod);",
                "      statement = new _ApplyClassRulesStatement_4(statement, description);",
                "      return statement;",
                "    }",
                "  }",
                "",
                "  private static class _Payloads_1 {",
                "    public org.junit.runners.model.Statement test;",
                "  }",
                "",
                "  @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)",
                "  public void makePayloads() {",
                "    this.payloads = new _Payloads_1();",
                "    this.payloads.test = _ClassStatement_3.forPayload(Test::test, \"test\", this);",
                "  }",
                "}"
        );
        UnitTestClass testClass = UnitTestClass.Builder.forClass("com.example.Test")
                .withTest("test")
                .withInstanceRuleField("rule")
                .withClassRuleField("classRule")
                .build();
        Predicate<String> nameValidator = n -> {
          switch (n) {
              case "_Benchmark":
              case "_Payloads":
              case "_Payloads_0":
              case "_InstanceStatement":
              case "_InstanceStatement_0":
              case "_InstanceStatement_1":
              case "_ClassStatement":
              case "_ClassStatement_0":
              case "_ClassStatement_1":
              case "_ClassStatement_2":
              case "_ApplyClassRulesStatement":
              case "_ApplyClassRulesStatement_0":
              case "_ApplyClassRulesStatement_1":
              case "_ApplyClassRulesStatement_2":
              case "_ApplyClassRulesStatement_3":
                  return false;
              default:
                  return true;
          }
        };

        ClassOrInterfaceDeclaration benchmark =
                TailoredBenchmarkFactory.generateBenchmarkClass(testClass, nameValidator);
        assertThat(benchmark, equalsAst(expected));
    }

    @Test
    public void generatedNameValidatorCatchesAllIdentifiers() {
        CompilationUnit compilationUnit = compilationUnitFromLines(
                "package _i0._i1;",
                "",
                "import _i2._i3;",
                "",
                "public class _i4<_i5 extends _i6 & _i7> extends _i8 {",
                "  public enum _i9 {",
                "    _i10",
                "  }",
                "",
                "  public @interface _i11 {",
                "    _i12 _i13();",
                "  }",
                "",
                "  static {",
                "    try (_i14 _i15 = _i16._i17()) {",
                "      _i18._i19();",
                "    } catch(_i20 | _i21 _i22) {",
                "      _i23();",
                "    } finally {",
                "      _i24();",
                "    }",
                "  }",
                "",
                "  private _i25<?> _i26 = _i27;",
                "  private _i28[] _i29 = new _i30[_i31];",
                "  private _i32[] _i33 = new _i34[] { _i35 };",
                "",
                "  @_i36(_i37)",
                "  public _i38() {",
                "    super();",
                "  }",
                "",
                "  @_i39(_i40=_i41, _i42=_i43)",
                "  public _i44 _i45(_i46 _i47) {",
                "    return _i48[_i49]._i50(_i51);",
                "  }",
                "",
                "  @_i52",
                "  public void _i53(_i54 this) {",
                "    _i55 _i56 = _i57 -> new _i58();",
                "    _i59 = _i60 + (_i61) super._i62;",
                "    _i63 = (_i64 instanceof _i65) ? _i66.class : this._i67._i68;",
                "    _i69 = \"NOT_i\";",
                "    _i70++;",
                "    class _i71 {}",
                "    _i72:",
                "    assert _i73 : _i74;",
                "    ;",
                "    switch (_i75) {",
                "      case _i76:",
                "        _i77();",
                "        break;",
                "      default:",
                "        _i78();",
                "    }",
                "    if (_i79) {",
                "      _i80();",
                "    } else if (_i81) {",
                "      throw _i82();",
                "    } else {",
                "      _i83();",
                "    }",
                "    while (_i84) {",
                "      if (_i85) {",
                "        continue;",
                "      }",
                "      _i86();",
                "    }",
                "    do {",
                "      _i87();",
                "    } while (_i88);",
                "    for (_i89 _i90 : _i91) {",
                "      _i92();",
                "    }",
                "    for (_i93 _i94 = _i95; _i96; _i97._i98()) {",
                "      _i99();",
                "    }",
                "    synchronized (_i100) {",
                "      _i101(_i102::_i103);",
                "    }",
                "    var _i104 = _i105;",
                "  }",
                "}"
        );

        Predicate<String> nameValidator =
                TailoredBenchmarkFactory.nameValidatorForCompilationUnit(compilationUnit);
        for (int i = 0; i <= 105; i++) {
            String identifier = "_i" + i;
            assertFalse(nameValidator.test(identifier), identifier + " passed validation");
        }
        assertTrue(nameValidator.test("_other"));
        assertTrue(nameValidator.test("_NOT_i"));
    }
}
