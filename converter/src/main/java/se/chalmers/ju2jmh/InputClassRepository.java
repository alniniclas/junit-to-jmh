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
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A repository for finding source code and bytecode of input classes.
 */
public class InputClassRepository {
    private final Map<String, InputClass> knownClasses = new HashMap<>();
    private final List<Path> sourcePath;
    private final List<Path> classPath;

    /**
     * Creates a new InputClassRepository reading sources and bytecode from the given root paths.
     *
     * @param sourcePaths Root paths of the source files that can be loaded by this repository.
     * @param classPath   Root paths of the class files that can be loaded by this repository.
     */
    public InputClassRepository(List<Path> sourcePaths, List<Path> classPath) {
        this.sourcePath = sourcePaths.stream().collect(Collectors.toUnmodifiableList());
        this.classPath = classPath.stream().collect(Collectors.toUnmodifiableList());
    }

    /**
     * Creates a new InputClassRepository reading sources and bytecode from the given root paths.
     *
     * @param sourcePath Root path of the source files that can be loaded by this repository.
     * @param classPath  Root path of the class files that can be loaded by this repository.
     */
    public InputClassRepository(Path sourcePath, Path classPath) {
        this.sourcePath = List.of(sourcePath);
        this.classPath = List.of(classPath);
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
        String sourceFileName = ClassNames.outermostClassName(name)
                .replace('.', File.separatorChar) + ".java";
        return findFirstExisting(sourcePath, sourceFileName)
                .orElseThrow(() ->
                        new ClassNotFoundException("Found no source file for class " + name));
    }

    /**
     * Returns a {@link InputClass} containing source code and bytecode for the class with the
     * given name, if present in this repository.
     *
     * @param name The name of the class to load source code and bytecode for.
     * @return An {@link InputClass} representing the requested class.
     * @throws ClassNotFoundException If the source code or bytecode for the given class name was
     * absent or could otherwise not be loaded.
     */
    public InputClass findClass(String name) throws ClassNotFoundException {
        InputClass inputClass = knownClasses.get(name);
        if (inputClass != null) {
            return inputClass;
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
        inputClass = new RepositoryInputClass(compilationUnit, bytecode);
        if (inputClass.getSource() == null) {
            throw new ClassNotFoundException("Failed to find class " + name + " in source file "
                    + sourceFile);
        }
        knownClasses.put(name, inputClass);
        return inputClass;
    }

    private static class RepositoryInputClass implements InputClass {
        private final CompilationUnit source;
        private final JavaClass bytecode;
        private final List<String> interfaceNames;

        public RepositoryInputClass(CompilationUnit source, JavaClass bytecode) {
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
            String typeName = ClassNames.shortClassName(getName());
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
