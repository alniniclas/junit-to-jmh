package se.chalmers.ju2jmh.experiments.basebenchmarks;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public class Workloads {

    private double workload1Input = 25.0;
    private double workload2Input = -12.345E-67;
    private String workload3Input = String.join("\n",
            "package com.example;",
            "",
            "public class Main {",
            "",
            "    public static void main(String[] args) {",
            "        System.out.println(\"Hello World!\");",
            "    }",
            "}"
    );

    @Benchmark
    public void baseline() {}

    @Benchmark
    public double workload1() {
        return Math.sqrt(workload1Input);
    }

    @Benchmark
    public String workload2() {
        return Double.toHexString(workload2Input);
    }

    @Benchmark
    public String workload3() {
        CompilationUnit parsed = StaticJavaParser.parse(workload3Input);
        TypeDeclaration<?> mainClass = parsed.getType(0);
        MethodDeclaration mainMethod = mainClass.getMember(0).asMethodDeclaration();
        MethodCallExpr printMethodCall = mainMethod.getBody()
                .get()
                .getStatement(0)
                .asExpressionStmt()
                .getExpression()
                .asMethodCallExpr();
        String helloWorld = printMethodCall.getArgument(0).asStringLiteralExpr().getValue();
        return helloWorld;
    }

    @Benchmark
    public void doubleWorkload1(Blackhole blackhole) {
        blackhole.consume(workload1());
        blackhole.consume(workload1());
    }

    @Benchmark
    public void doubleWorkload2(Blackhole blackhole) {
        blackhole.consume(workload2());
        blackhole.consume(workload2());
    }

    @Benchmark
    public void doubleWorkload3(Blackhole blackhole) {
        blackhole.consume(workload3());
        blackhole.consume(workload3());
    }
}
