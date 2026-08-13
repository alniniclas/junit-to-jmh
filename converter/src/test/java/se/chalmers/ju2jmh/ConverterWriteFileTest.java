package se.chalmers.ju2jmh;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterWriteFileTest {

    private CompilationUnit makeBenchmarkWithMethod(String className, String methodName, String... imports) {
        StringBuilder sb = new StringBuilder();
        sb.append("package se.generated;\n");
        if (imports != null) {
            for (String im : imports) {
                sb.append("import ").append(im).append(";\n");
            }
        }
        sb.append("public class ").append(className).append(" { public void ")
                .append(methodName).append("() { /* empty */ } }");
        return StaticJavaParser.parse(sb.toString());
    }

    private void callWrite(Object converterInstance, CompilationUnit cu, File out) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = Converter.class.getDeclaredMethod("writeSourceCodeToFile", CompilationUnit.class, File.class);
        m.setAccessible(true);
        m.invoke(converterInstance, cu, out);
    }

    private void setOnConflict(Object converterInstance, String policy) throws NoSuchFieldException, IllegalAccessException {
        Field f = Converter.class.getDeclaredField("onConflict");
        f.setAccessible(true);
        f.set(converterInstance, policy);
    }

    @Test
    public void testCreateThenMergeKeepsPrevious(@TempDir Path tempDir) throws Exception {
        Converter conv = new Converter();
        CompilationUnit a = makeBenchmarkWithMethod("BankBench", "benchmark_a");
        File out = tempDir.resolve("se/generated/BankBench.java").toFile();

        // create
        callWrite(conv, a, out);
        assertTrue(out.exists());
        String contentA = Files.readString(out.toPath());
        assertTrue(contentA.contains("benchmark_a"));

        // merge
        CompilationUnit b = makeBenchmarkWithMethod("BankBench", "benchmark_b");
        setOnConflict(conv, "merge");
        callWrite(conv, b, out);
        String contentMerged = Files.readString(out.toPath());
        assertTrue(contentMerged.contains("benchmark_a"), "Merged file should keep benchmark_a");
        assertTrue(contentMerged.contains("benchmark_b"), "Merged file should contain benchmark_b");
    }

    @Test
    public void testBackupCreatedOnOverwrite(@TempDir Path tempDir) throws Exception {
        Converter conv = new Converter();
        CompilationUnit a = makeBenchmarkWithMethod("BankBench", "benchmark_a");
        File out = tempDir.resolve("se/generated/BankBench.java").toFile();

        // initial create
        callWrite(conv, a, out);
        assertTrue(out.exists());

        // overwrite with b
        CompilationUnit b = makeBenchmarkWithMethod("BankBench", "benchmark_b");
        setOnConflict(conv, "overwrite");
        callWrite(conv, b, out);

        Path backup = out.toPath().resolveSibling(out.getName() + ".bak");
        assertTrue(Files.exists(backup), "Backup file should exist");
        String bakContent = Files.readString(backup);
        assertTrue(bakContent.contains("benchmark_a"), "Backup should contain previous content with benchmark_a");

        String newContent = Files.readString(out.toPath());
        assertFalse(newContent.contains("benchmark_a"));
        assertTrue(newContent.contains("benchmark_b"));
    }

    @Test
    public void testAvoidDuplicateImportsOnMerge(@TempDir Path tempDir) throws Exception {
        Converter conv = new Converter();

        // existing file has import java.util.List
        String existingSrc = "package se.generated;\nimport java.util.List;\npublic class BankBench { public void benchmark_a() {} }\n";
        File out = tempDir.resolve("se/generated/BankBench.java").toFile();
        out.getParentFile().mkdirs();
        Files.writeString(out.toPath(), existingSrc);

        // new CU also declares import java.util.List
        CompilationUnit newCu = makeBenchmarkWithMethod("BankBench", "benchmark_b", "java.util.List");
        setOnConflict(conv, "merge");
        callWrite(conv, newCu, out);

        String result = Files.readString(out.toPath());
        // count occurrences of the import line
        Pattern p = Pattern.compile("import\\s+java\\.util\\.List\\s*;", Pattern.MULTILINE);
        Matcher m = p.matcher(result);
        int count = 0;
        while (m.find()) count++;
        assertEquals(1, count, "There should be exactly one import java.util.List; after merge");
        assertTrue(result.contains("benchmark_a"));
        assertTrue(result.contains("benchmark_b"));
    }

    @Test
    public void testFallbackOnParseErrorCreatesBackupAndOverwrites(@TempDir Path tempDir) throws Exception {
        Converter conv = new Converter();
        // create an invalid existing file
        File out = tempDir.resolve("se/generated/BankBench.java").toFile();
        out.getParentFile().mkdirs();
        String broken = "THIS IS NOT JAVACODE\npublic class X {";
        Files.writeString(out.toPath(), broken);

        // new CU
        CompilationUnit newCu = makeBenchmarkWithMethod("BankBench", "benchmark_b");
        setOnConflict(conv, "merge");

        callWrite(conv, newCu, out);

        Path backup = out.toPath().resolveSibling(out.getName() + ".bak");
        assertTrue(Files.exists(backup), "Backup should be created when existing file is unparsable");
        String bakContent = Files.readString(backup);
        assertTrue(bakContent.contains("THIS IS NOT JAVACODE"));

        String newContent = Files.readString(out.toPath());
        assertTrue(newContent.contains("benchmark_b"));
    }
}
