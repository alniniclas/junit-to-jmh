package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A repository for finding source code and bytecode of input classes.
 */
public class SourceClassRepository {
    private final Map<String, SourceClass> knownClasses = new HashMap<>();
    private final List<Path> sourcePath;
    private final List<Path> classPath;

    private static List<Path> toPaths(String pathString) {
        return Arrays.stream(pathString.split(File.pathSeparator))
                .map(p -> Path.of(p))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new SourceClassRepository reading sources and bytecode from the given root paths.
     *
     * @param sourcePath Root path(s) of the source files that can be loaded by this repository,
     *                  separated by {@link File#pathSeparator}.
     * @param classPath Root path(s) of the class files that can be loaded by this repository,
     *                 separated by {@link File#pathSeparator}.
     */
    public SourceClassRepository(String sourcePath, String classPath) {
        this.sourcePath = toPaths(sourcePath);
        this.classPath = toPaths(classPath);
    }

    private static Optional<Path> findFirstExisting(List<Path> basePaths, String relativePath) {
        return basePaths.stream()
                .map(p -> p.resolve(relativePath))
                .filter(f -> f.toFile().exists())
                .findFirst();
    }

    private Path findBytecodeFile(String name) throws ClassNotFoundException {
        String bytecodeFileName = name.replace('.', File.separatorChar) + ".class";
        return findFirstExisting(classPath, bytecodeFileName)
                .orElseThrow(() ->
                        new ClassNotFoundException("Found no bytecode for class " + name));
    }

    private Path findSourceFile(String name) throws ClassNotFoundException {
        String outerClassName = name.indexOf('$') < 0 ? name : name.substring(0, name.indexOf('$'));
        String outerClassSourceFileName =
                outerClassName.replace('.', File.separatorChar) + ".java";
        return findFirstExisting(sourcePath, outerClassSourceFileName)
                .orElseThrow(() ->
                        new ClassNotFoundException("Found no source file for class " + name));
    }

    /**
     * Returns a {@link SourceClass} containing source code and bytecode for the class with the
     * given name, if present in this repository.
     *
     * @param name The name of the class to load source code and bytecode for.
     * @return A {@link SourceClass} representing the requested class.
     * @throws ClassNotFoundException If the source code or bytecode for the given class name was
     * absent or could otherwise not be loaded.
     */
    public SourceClass findClass(String name) throws ClassNotFoundException {
        SourceClass sourceClass = knownClasses.get(name);
        if (sourceClass != null) {
            return sourceClass;
        }
        Path bytecodeFile = findBytecodeFile(name);
        JavaClass bytecode;
        try {
            bytecode = new ClassParser(bytecodeFile.toString()).parse();
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to read bytecode for class " + name
                    + " from file " + bytecodeFile, e);
        }
        Path sourceFile = findSourceFile(name);
        CompilationUnit compilationUnit;
        try {
            compilationUnit = StaticJavaParser.parse(sourceFile);
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to read source for class " + name
                    + " from source file " + sourceFile, e);
        }
        sourceClass = new RepositorySourceClass(compilationUnit, bytecode);
        if (sourceClass.getSource() == null) {
            throw new ClassNotFoundException("Failed to find class " + name + " in source file "
                    + sourceFile);
        }
        knownClasses.put(name, sourceClass);
        return sourceClass;
    }

    private static class RepositorySourceClass implements SourceClass {
        private final CompilationUnit source;
        private final JavaClass bytecode;
        private final List<String> interfaceNames;

        public RepositorySourceClass(CompilationUnit source, JavaClass bytecode) {
            this.source = source;
            this.bytecode = bytecode;
            interfaceNames = List.of(bytecode.getInterfaceNames());
        }

        @Override
        public String getName() {
            return bytecode.getClassName();
        }

        private TypeDeclaration<?> findTypeDeclaration(
                Stream<TypeDeclaration<?>> types, String simpleName) {
            return types.filter(t -> t.getName().asString().equals(simpleName))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public TypeDeclaration<?> getSource() {
            CompilationUnit compilationUnit = source.clone();
            String typeName = getName();
            typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
            Queue<String> typeNames = new ArrayDeque<>(Arrays.asList(typeName.split("\\$")));
            TypeDeclaration<?> type =
                    findTypeDeclaration(compilationUnit.getTypes().stream(), typeNames.remove());
            while (type != null && !typeNames.isEmpty()) {
                Stream<TypeDeclaration<?>> typeDeclarations =
                        type.getMembers().stream()
                                .filter(BodyDeclaration::isTypeDeclaration)
                                .map(t -> (TypeDeclaration<?>) t);
                type = findTypeDeclaration(typeDeclarations, typeNames.remove());
            }
            return type;
        }

        @Override
        public JavaClass getBytecode() {
            return bytecode.copy();
        }

        @Override
        public String getSuperclassName() {
            return bytecode.getSuperclassName();
        }

        @Override
        public List<String> getInterfaceNames() {
            return interfaceNames;
        }
    }
}
