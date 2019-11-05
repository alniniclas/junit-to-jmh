package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;

public class AstResourceLoader {
    private final String rootPath;

    public AstResourceLoader(Class<?> resourceRoot) {
        this.rootPath = resourceRoot.getCanonicalName().replace('.', '/');
    }

    public CompilationUnit load(String resource) throws IOException {
        return StaticJavaParser.parseResource(rootPath + '/' + resource);
    }
}
