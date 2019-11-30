package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

import java.util.List;
import java.util.stream.Collectors;

public class JmhFixtureImprover {

    private JmhFixtureImprover() {
        throw new AssertionError("Should not be instantiated.");
    }

    private static boolean isLevelInvocation(AnnotationExpr annotation) {
        if (!annotation.isSingleMemberAnnotationExpr()) {
            return false;
        }
        Expression value = annotation.asSingleMemberAnnotationExpr().getMemberValue();
        if (value.isFieldAccessExpr()) {
            return value.asFieldAccessExpr().getNameAsString().equals(Level.Invocation.name());
        } else if (value.isNameExpr()) {
            return value.asNameExpr().getNameAsString().equals(Level.Invocation.name());
        }
        return false;
    }

    private static void addMethodCalls(BlockStmt block, List<MethodDeclaration> methods) {
        for (MethodDeclaration method : methods) {
            Expression methodCall = new MethodCallExpr().setName(method.getName());
            block.addStatement(methodCall);
        }
    }

    private static void injectFixtureSetUpCalls(MethodDeclaration benchmarkMethod,
                                                List<MethodDeclaration> setUpMethods) {
        BlockStmt body = benchmarkMethod.getBody().get();
        int index = 0;
        for (MethodDeclaration setUpMethod : setUpMethods) {
            Expression methodCall = new MethodCallExpr().setName(setUpMethod.getName());
            body.addStatement(index, methodCall);
            index++;
        }
    }

    private static void injectFixtureCalls(
            MethodDeclaration benchmarkMethod, List<MethodDeclaration> invocationSetUpMethods,
            List<MethodDeclaration> invocationTearDownMethods) {
        BlockStmt newBody = new BlockStmt();
        addMethodCalls(newBody, invocationSetUpMethods);
        BlockStmt finallyBlock = new BlockStmt();
        addMethodCalls(finallyBlock, invocationTearDownMethods);
        newBody.addStatement(new TryStmt()
                .setTryBlock(benchmarkMethod.getBody().get())
                .setFinallyBlock(finallyBlock));
        benchmarkMethod.setBody(newBody);
    }

    public static void improveFixture(CompilationUnit compilationUnit) {
        for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
            List<MethodDeclaration> invocationSetUpMethods = type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(Setup.class))
                    .filter(m -> isLevelInvocation(m.getAnnotationByClass(Setup.class).get()))
                    .collect(Collectors.toUnmodifiableList());
            List<MethodDeclaration> invocationTearDownMethods = type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(TearDown.class))
                    .filter(m -> isLevelInvocation(m.getAnnotationByClass(TearDown.class).get()))
                    .collect(Collectors.toUnmodifiableList());
            List<MethodDeclaration> benchmarkMethods = type.getMethods().stream()
                    .filter(m -> m.isAnnotationPresent(Benchmark.class))
                    .collect(Collectors.toUnmodifiableList());
            for (MethodDeclaration benchmarkMethod : benchmarkMethods) {
                if (invocationTearDownMethods.isEmpty()) {
                    injectFixtureSetUpCalls(benchmarkMethod, invocationSetUpMethods);
                } else {
                    injectFixtureCalls(
                            benchmarkMethod, invocationSetUpMethods, invocationTearDownMethods);
                }
            }
            invocationSetUpMethods.forEach(
                    m -> m.getAnnotations().remove(m.getAnnotationByClass(Setup.class).get()));
            invocationTearDownMethods.forEach(
                    m -> m.getAnnotations().remove(m.getAnnotationByClass(TearDown.class).get()));
        }
    }
}
