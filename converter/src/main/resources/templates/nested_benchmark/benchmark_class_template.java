@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public static class BENCHMARK_TEMPLATE extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
    @java.lang.Override
    public void beforeClass() throws java.lang.Throwable {
        super.beforeClass();
    }

    @java.lang.Override
    public void afterClass() throws java.lang.Throwable {
        super.afterClass();
    }

    @java.lang.Override
    public void before() throws java.lang.Throwable {
        super.before();
    }

    @java.lang.Override
    public void after() throws java.lang.Throwable {
        super.after();
    }

    @java.lang.Override
    public org.junit.runners.model.Statement applyClassRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
        statement = super.applyClassRuleFields(statement, description);
        return statement;
    }

    @java.lang.Override
    public org.junit.runners.model.Statement applyClassRuleMethods(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
        statement = super.applyClassRuleMethods(statement, description);
        return statement;
    }

    @java.lang.Override
    public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
        statement = super.applyRuleFields(statement, description);
        return statement;
    }

    @java.lang.Override
    public org.junit.runners.model.Statement applyRuleMethods(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
        statement = super.applyRuleMethods(statement, description);
        return statement;
    }

    private IMPLEMENTATION_CLASS_NAME implementation;

    @java.lang.Override
    public void createImplementation() throws java.lang.Throwable {
        this.implementation = new IMPLEMENTATION_CLASS_NAME();
    }

    @java.lang.Override
    public IMPLEMENTATION_CLASS_NAME implementation() {
        return this.implementation;
    }
}
