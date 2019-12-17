package se.chalmers.ju2jmh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public final class InputClassDirectory {
    private final Path sourcesDirectory;
    private final Path bytecodeDirectory;

    public InputClassDirectory(Path baseDirectory) {
        sourcesDirectory = baseDirectory.resolve("src");
        bytecodeDirectory = baseDirectory.resolve("classes");
    }

    public static InputClassDirectory directoryWithClasses(Path baseDirectory, Class<?>... classes)
            throws IOException, ClassNotFoundException {
        InputClassDirectory directory = new InputClassDirectory(baseDirectory);
        for (Class<?> clazz : classes) {
            directory.add(clazz);
        }
        return directory;
    }

    private static void copyResourceToFile(ClassLoader classLoader, String resource, Path basePath)
            throws IOException, ClassNotFoundException {
        File outputFile = basePath.resolve(resource.replace('/', File.separatorChar)).toFile();
        if (outputFile.exists()) {
            return;
        }
        outputFile.getParentFile().mkdirs();
        outputFile.createNewFile();
        try (InputStream in = classLoader.getResourceAsStream(resource)) {
            if (in == null) {
                throw new ClassNotFoundException("Could not find resource " + resource);
            }
            try (OutputStream out = new FileOutputStream(outputFile)) {
                in.transferTo(out);
            }
        }
    }

    private static String sourceResourceName(Class<?> clazz) {
        return ClassNames.outermostClassName(clazz).replace('.', '/') + ".java";
    }

    private static String bytecodeResourceName(Class<?> clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }

    public void add(Class<?> clazz) throws IOException, ClassNotFoundException {
        addSource(clazz);
        addBytecode(clazz);
    }

    public void addSource(Class<?> clazz) throws IOException, ClassNotFoundException {
        copyResourceToFile(clazz.getClassLoader(), sourceResourceName(clazz), sourcesDirectory);
    }

    public void addBytecode(Class<?> clazz) throws IOException, ClassNotFoundException {
        copyResourceToFile(clazz.getClassLoader(), bytecodeResourceName(clazz), bytecodeDirectory);
    }

    public Path sourcesDirectory() {
        return sourcesDirectory;
    }

    public Path bytecodeDirectory() {
        return bytecodeDirectory;
    }
}
