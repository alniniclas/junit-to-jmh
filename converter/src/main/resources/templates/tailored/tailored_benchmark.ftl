@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public static class ${className} {
    private ${payloadsClassName} payloads;
    private ${testClassName} instance;

    <#list benchmarks as benchmark>
    @org.openjdk.jmh.annotations.Benchmark
    public void benchmark_${benchmark.testName}() throws java.lang.Throwable {
        <#if !hasRules>
        this.runBenchmark(this.payloads.${benchmark.testName});
        <#else>
        this.payloads.${benchmark.testName}.evaluate();
        </#if>
    }

    </#list>
    <#macro classFixture>
        <#list beforeClassMethods as beforeClass>
        ${beforeClass.className}.${beforeClass.name}();
        </#list>
        <#list afterClassMethods>
        try {
            <#nested>
        } finally {
            <#items as afterClass>
            ${afterClass.className}.${afterClass.name}();
            </#items>
        }
        <#else>
            <#nested>
        </#list>
    </#macro>
    <#macro instanceFixture instanceReference>
        <#list beforeMethods as before>
        ${instanceReference}.${before.name}();
        </#list>
        <#list afterMethods>
        try {
            <#nested>
        } finally {
            <#items as after>
            ${instanceReference}.${after.name}();
            </#items>
        }
        <#else>
            <#nested>
        </#list>
    </#macro>
    <#if !hasRules>
    private void runBenchmark(se.chalmers.ju2jmh.api.ThrowingConsumer<${testClassName}> payload) throws java.lang.Throwable {
        <@classFixture>
            this.instance = new ${testClassName}();
            <@instanceFixture instanceReference="this.instance">
                payload.accept(this.instance);
            </@instanceFixture>
        </@classFixture>
    }

    </#if>
    <#if hasInstanceRules>
    private static class ${instanceStatementClassName} extends org.junit.runners.model.Statement {
        private final se.chalmers.ju2jmh.api.ThrowingConsumer<${testClassName}> payload;
        private final ${className} benchmark;

        public ${instanceStatementClassName}(
                se.chalmers.ju2jmh.api.ThrowingConsumer<${testClassName}> payload, ${className} benchmark) {
            this.payload = payload;
            this.benchmark = benchmark;
        }

        @java.lang.Override
        public void evaluate() throws java.lang.Throwable {
            <@instanceFixture instanceReference="this.benchmark.instance">
                this.payload.accept(this.benchmark.instance);
            </@instanceFixture>
        }
    }

    </#if>
    <#if hasRules>
    private static class ${classStatementClassName} extends org.junit.runners.model.Statement {
        private final se.chalmers.ju2jmh.api.ThrowingConsumer<${testClassName}> payload;
        private final ${className} benchmark;
        <#if hasInstanceRules>
        private final org.junit.runner.Description description;
        private final org.junit.runners.model.FrameworkMethod frameworkMethod;
        </#if>

        private ${classStatementClassName}(
                se.chalmers.ju2jmh.api.ThrowingConsumer<${testClassName}> payload,
                ${className} benchmark<#if hasInstanceRules>,
                org.junit.runner.Description description,
                org.junit.runners.model.FrameworkMethod frameworkMethod</#if>) {
            this.payload = payload;
            this.benchmark = benchmark;
            <#if hasInstanceRules>
            this.description = description;
            this.frameworkMethod = frameworkMethod;
            </#if>
        }

        @java.lang.Override
        public void evaluate() throws java.lang.Throwable {
            <@classFixture>
                this.benchmark.instance = new ${testClassName}();
                <#list instanceRules>
                org.junit.runners.model.Statement statement =
                    new ${instanceStatementClassName}(this.payload, this.benchmark);
                    <#items as instanceRule>
                    statement = this.applyRule(
                        <#if instanceRule.hidden>
                            ((${instanceRule.className}) this.benchmark.instance)
                         <#else>
                            this.benchmark.instance
                         </#if>.${instanceRule.name}<#if instanceRule.fromMethod>()</#if>,
                         statement);
                    </#items>
                    statement.evaluate();
                <#else>
                    <@instanceFixture instanceReference="this.benchmark.instance">
                    this.payload.accept(this.benchmark.instance);
                    </@instanceFixture>
                </#list>
            </@classFixture>
        }

        <#if hasInstanceRules>
        private org.junit.runners.model.Statement applyRule(
                org.junit.rules.TestRule rule, org.junit.runners.model.Statement statement) {
            return se.chalmers.ju2jmh.api.Rules.apply(rule, statement, this.description);
        }

        private org.junit.runners.model.Statement applyRule(
                org.junit.rules.MethodRule rule, org.junit.runners.model.Statement statement) {
            return se.chalmers.ju2jmh.api.Rules.apply(rule, statement, this.frameworkMethod, this.benchmark.instance);
        }

        </#if>
        <#if hasClassRules>
        private static class ${applyClassRulesStatementClassName} extends org.junit.runners.model.Statement {
            private final org.junit.runners.model.Statement statement;
            private final org.junit.runner.Description description;

            public ${applyClassRulesStatementClassName}(
                    org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
                this.statement = statement;
                this.description = description;
            }

            @java.lang.Override
            public void evaluate() throws java.lang.Throwable {
                org.junit.runners.model.Statement statement = this.statement;
                <#list classRules as classRule>
                statement = se.chalmers.ju2jmh.api.Rules.apply(
                    ${classRule.className}.${classRule.name}<#if classRule.fromMethod>()</#if>, statement, this.description);
                </#list>
                statement.evaluate();
            }
        }

        </#if>
        public static org.junit.runners.model.Statement forPayload(
                se.chalmers.ju2jmh.api.ThrowingConsumer<${testClassName}> payload,
                String name,
                ${className} benchmark) {
            org.junit.runner.Description description =
                se.chalmers.ju2jmh.api.Rules.description(${testClassName}.class, name);
            <#if hasInstanceRules>
            org.junit.runners.model.FrameworkMethod frameworkMethod =
                se.chalmers.ju2jmh.api.Rules.frameworkMethod(${testClassName}.class, name);
            </#if>
            org.junit.runners.model.Statement statement =
                new ${classStatementClassName}(
                    payload, benchmark<#if hasInstanceRules>, description, frameworkMethod</#if>);
            <#if hasClassRules>
            statement = new ${applyClassRulesStatementClassName}(statement, description);
            </#if>
            return statement;
        }
    }

    </#if>
    private static class ${payloadsClassName} {
        <#list benchmarks as benchmark>
            <#if !hasRules>
            public se.chalmers.ju2jmh.api.ThrowingConsumer<${testClassName}> ${benchmark.testName};
            <#else>
            public org.junit.runners.model.Statement ${benchmark.testName};
            </#if>
        </#list>
    }

    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Trial)
    public void makePayloads() {
        this.payloads = new ${payloadsClassName}();
        <#macro payloadReference benchmark>
            <#if !benchmark.exceptionTest>
            ${testClassName}::${benchmark.testName}
            <#else>
            new se.chalmers.ju2jmh.api.ExceptionTest<>(${testClassName}::${benchmark.testName}, ${benchmark.expectedException}.class)
            </#if>
        </#macro>
        <#list benchmarks as benchmark>
        this.payloads.${benchmark.testName} =
            <#if !hasRules>
            <@payloadReference benchmark />;
            <#else>
            ${classStatementClassName}.forPayload(<@payloadReference benchmark />, "${benchmark.testName}", this);
            </#if>
        </#list>
    }
}
