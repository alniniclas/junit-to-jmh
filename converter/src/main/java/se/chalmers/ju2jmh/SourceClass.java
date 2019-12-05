package se.chalmers.ju2jmh;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.apache.bcel.classfile.JavaClass;

import java.util.List;

public interface SourceClass {
    String getName();
    SourceTypeDeclaration getSource();
    JavaClass getBytecode();
    SourceClass getSuperclass();
    List<SourceClass> getKnownInterfaces();

    interface SourceTypeDeclaration {
        CompilationUnit compilationUnit();
        TypeDeclaration<?> typeDeclaration();
    }
}
