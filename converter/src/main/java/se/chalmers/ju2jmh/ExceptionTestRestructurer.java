package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import se.chalmers.ju2jmh.api.ExceptionTest;

import java.util.Optional;

public class ExceptionTestRestructurer {
    private static final String TEST = "Test";
    private static final String EXPECTED = "expected";

    private ExceptionTestRestructurer() {
        throw new AssertionError("Should not be instantiated.");
    }

    public static void restructureExceptionTests(CompilationUnit compilationUnit) {
        for (TypeDeclaration<?> type : compilationUnit.getTypes()) {
            for (MethodDeclaration method : type.getMethods()) {
                Optional<AnnotationExpr> maybeTestAnnotation = method.getAnnotationByName(TEST);
                if (maybeTestAnnotation.isEmpty()
                        || !maybeTestAnnotation.get().isNormalAnnotationExpr()
                        || method.getBody().isEmpty()) {
                    continue;
                }
                NormalAnnotationExpr testAnnotation =
                        maybeTestAnnotation.get().asNormalAnnotationExpr();
                Optional<MemberValuePair> maybeExpected = testAnnotation.getPairs().stream()
                        .filter(p -> p.getName().asString().equals(EXPECTED))
                        .findAny();
                if (maybeExpected.isEmpty()) {
                    continue;
                }
                MemberValuePair expected = maybeExpected.get();
                testAnnotation.getPairs().remove(expected);
                method.setThrownExceptions(new NodeList<>());
                BlockStmt oldBody = method.getBody().get();
                BlockStmt newBody = new BlockStmt();
                MethodCallExpr assertThrowsCall = new MethodCallExpr().setName("assertThrows")
                        .setScope(StaticJavaParser.parseExpression(
                                ExceptionTest.class.getCanonicalName()));
                NodeList<Expression> arguments = new NodeList<>();
                arguments.add(expected.getValue());
                arguments.add(new LambdaExpr().setEnclosingParameters(true).setBody(oldBody));
                assertThrowsCall.setArguments(arguments);
                newBody.addStatement(assertThrowsCall);
                method.setBody(newBody);
                if (testAnnotation.getPairs().isEmpty()) {
                    method.getAnnotations().replace(testAnnotation,
                            new MarkerAnnotationExpr().setName(testAnnotation.getName()));
                }
            }
        }
    }
}
