package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

public class TestCaseExtractor {
    private static final String TEST_ANNOTATION_NAME = "Test";

    private TestCaseExtractor() {
        throw new AssertionError("Should not be instantiated.");
    }

    public static CompilationUnit extractTestMethods(CompilationUnit compilationUnit) {
        CompilationUnit extracted = compilationUnit.clone();
        for (TypeDeclaration<?> type : extracted.getTypes()) {
            type.getMethods().stream()
                    .filter(m -> !m.isAnnotationPresent(TEST_ANNOTATION_NAME))
                    .forEach(type::remove);
        }
        return extracted;
    }
}
