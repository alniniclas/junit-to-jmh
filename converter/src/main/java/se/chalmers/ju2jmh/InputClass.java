package se.chalmers.ju2jmh;

import com.github.javaparser.ast.body.TypeDeclaration;
import org.apache.bcel.classfile.JavaClass;

import java.util.List;

/**
 * A Java class with source code and bytecode, used as input for the converter.
 */
public interface InputClass {
    /**
     * Returns the name of this class.
     */
    String getName();

    /**
     * Returns a copy of the source type declaration for this class. A copy of the containing
     * compilation unit (which is guaranteed to be present) can be obtained by calling
     * {@link TypeDeclaration#findCompilationUnit()} on the returned type declaration.
     */
    TypeDeclaration<?> getSource();

    /**
     * Returns a copy of the bytecode for this class.
     */
    JavaClass getBytecode();

    /**
     * Returns the name of this class' superclass.
     */
    String getSuperclassName();

    /**
     * Returns the names of all interfaces implemented by this class.
     */
    List<String> getInterfaceNames();
}
