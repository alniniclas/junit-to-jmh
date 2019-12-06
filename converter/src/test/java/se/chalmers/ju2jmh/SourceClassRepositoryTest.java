package se.chalmers.ju2jmh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.testinput.*;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

public class SourceClassRepositoryTest {
    private SourceClassDirectory sourceClassDirectory;

    @BeforeEach
    public void setUpSourceClassDir(@TempDir Path tempDir) {
        sourceClassDirectory = new SourceClassDirectory(tempDir);
    }

    private SourceClassRepository makeRepository(Class<?>... classes)
            throws IOException, ClassNotFoundException {
        for (Class<?> clazz : classes) {
            sourceClassDirectory.add(clazz);
        }
        return new SourceClassRepository(sourceClassDirectory.sourcesDirectory().toString(),
                sourceClassDirectory.bytecodeDirectory().toString());
    }

    @Test
    public void findsClassWithBytecodeAndSources() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository();
        sourceClassDirectory.add(SimpleClass.class);
        SourceClass simpleClass = repository.findClass(SimpleClass.class.getName());
        assertEquals(SimpleClass.class.getName(), simpleClass.getName());
    }

    @Test
    public void failsToFindAbsentClass() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository();
        assertThrows(ClassNotFoundException.class,
                () -> repository.findClass(SimpleClass.class.getName()));
    }

    @Test
    public void failsToFindClassWithAbsentSource() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository();
        sourceClassDirectory.addBytecode(SimpleClass.class);
        assertThrows(ClassNotFoundException.class,
                () -> repository.findClass(SimpleClass.class.getName()));
    }

    @Test
    public void failsToFindClassWithAbsentBytecode() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository();
        sourceClassDirectory.addSource(SimpleClass.class);
        assertThrows(ClassNotFoundException.class,
                () -> repository.findClass(SimpleClass.class.getName()));
    }

    @Test
    public void returnsSameObject() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository(SimpleClass.class);
        SourceClass first = repository.findClass(SimpleClass.class.getName());
        SourceClass second = repository.findClass(SimpleClass.class.getName());
        assertSame(first, second);
    }

    @Test
    public void findsPresentSuperclass() throws IOException, ClassNotFoundException {
        SourceClassRepository repository =
                makeRepository(SimpleSubclass.class, SimpleSuperclass.class);
        SourceClass subclass = repository.findClass(SimpleSubclass.class.getName());
        SourceClass superclass = subclass.getSuperclass();
        assertNotNull(superclass);
        assertEquals(SimpleSuperclass.class.getName(), superclass.getName());
    }

    @Test
    public void failsToFindAbsentSuperclass() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository(SimpleSubclass.class);
        SourceClass subclass = repository.findClass(SimpleSubclass.class.getName());
        SourceClass superclass = subclass.getSuperclass();
        assertNull(superclass);
    }

    @Test
    public void findsNestedClasses() throws IOException, ClassNotFoundException {
        Class<?>[] classes = new Class<?>[] {
                NestedClasses.class, NestedClasses.Interface.class, NestedClasses.Static.class,
                NestedClasses.StaticAbstract.class, NestedClasses.StaticWithStatic.class,
                NestedClasses.StaticWithStatic.StaticStatic.class, NestedClasses.Inner.class,
                NestedClasses.AbstractInner.class, NestedClasses.InnerWithInner.class,
                NestedClasses.InnerWithInner.InnerInner.class
        };
        SourceClassRepository repository = makeRepository(classes);
        for (Class<?> clazz : classes) {
            SourceClass sourceClass = repository.findClass(clazz.getName());
            assertEquals(clazz.getName(), sourceClass.getName());
            String sourceSimpleName =
                    sourceClass.getSource().typeDeclaration().getName().asString();
            assertEquals(clazz.getSimpleName(), sourceSimpleName);
        }
    }

    private void findsInterfaces(Class<?> implementingClass, Class<?>... interfaceClasses)
            throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository(implementingClass);
        for (Class<?> clazz : interfaceClasses) {
            sourceClassDirectory.add(clazz);
        }
        SourceClass implementingSourceClass = repository.findClass(implementingClass.getName());
        List<String> interfaceNames = implementingSourceClass.getKnownInterfaces().stream()
                .map(SourceClass::getName)
                .collect(Collectors.toUnmodifiableList());
        assertThat(interfaceNames,
                containsInAnyOrder(Arrays.stream(interfaceClasses).map(Class::getName).toArray()));
    }

    @Test
    public void findsTwoPresentInterfaces() throws IOException, ClassNotFoundException {
        findsInterfaces(
                SimpleClassWithInterfaces.class, SimpleInterface1.class, SimpleInterface2.class);
    }

    @Test
    public void findsOnePresentInterface() throws IOException, ClassNotFoundException {
        findsInterfaces(SimpleClassWithInterfaces.class, SimpleInterface1.class);
    }

    @Test
    public void findsNoPresentInterfaces() throws IOException, ClassNotFoundException {
        findsInterfaces(SimpleClassWithInterfaces.class);
    }
}
