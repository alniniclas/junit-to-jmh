package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;

public class AstResources {
    private AstResources() {
        throw new AssertionError("Should not be instantiated.");
    }

    public static CompilationUnit loadAst(Class<?> resourceRoot, String resource)
            throws IOException {
        String rootPath = resourceRoot.getCanonicalName().replace('.', '/');
        return StaticJavaParser.parseResource(rootPath + '/' + resource);
    }
}
