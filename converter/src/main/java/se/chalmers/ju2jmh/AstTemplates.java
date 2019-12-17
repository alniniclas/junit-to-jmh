package se.chalmers.ju2jmh;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AstTemplates {
    private AstTemplates() {
        throw new AssertionError("Should not be instantiated.");
    }

    public static class AstTemplateException extends RuntimeException {
        public AstTemplateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static String loadTemplate(String templateName) {
        @SuppressWarnings("UnstableApiUsage")
        URL resourceUrl = Resources.getResource(templateName);
        try {
            @SuppressWarnings("UnstableApiUsage")
            String template = Resources.toString(resourceUrl, StandardCharsets.UTF_8);
            return template;
        } catch (IOException e) {
            throw new AstTemplateException("Failed to load AST template " + templateName, e);
        }
    }

    public static CompilationUnit compilationUnit(String templateName) {
        try {
            return StaticJavaParser.parse(loadTemplate(templateName));
        } catch (ParseProblemException e) {
            throw new AstTemplateException(
                    "Failed to parse compilation unit template " + templateName, e);
        }
    }

    public static MethodDeclaration method(String templateName) {
        try {
            return StaticJavaParser.parseMethodDeclaration(loadTemplate(templateName));
        } catch (ParseProblemException e) {
            throw new AstTemplateException("Failed to parse method template " + templateName, e);
        }
    }

    public static TypeDeclaration<?> type(String templateName) {
        try {
            return StaticJavaParser.parseTypeDeclaration(loadTemplate(templateName));
        } catch (ParseProblemException e) {
            throw new AstTemplateException("Failed to parse type template " + templateName, e);
        }
    }
}
