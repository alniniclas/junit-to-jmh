package se.chalmers.ju2jmh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.testinput.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
    public void findsPresentSuperclassName() throws IOException, ClassNotFoundException {
        SourceClassRepository repository =
                makeRepository(SimpleSubclass.class, SimpleSuperclass.class);
        SourceClass subclass = repository.findClass(SimpleSubclass.class.getName());
        String superclassName = subclass.getSuperclassName();
        assertEquals(SimpleSuperclass.class.getName(), superclassName);
    }

    @Test
    public void findsAbsentSuperclassName() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository(SimpleSubclass.class);
        SourceClass subclass = repository.findClass(SimpleSubclass.class.getName());
        String superclassName = subclass.getSuperclassName();
        assertEquals(SimpleSuperclass.class.getName(), superclassName);
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
                    sourceClass.getSource().getName().asString();
            assertEquals(clazz.getSimpleName(), sourceSimpleName);
        }
    }

    @Test
    public void findsPresentInterfaceNames() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository(
                SimpleClassWithInterfaces.class, SimpleInterface1.class, SimpleInterface2.class);
        SourceClass implementingSourceClass =
                repository.findClass(SimpleClassWithInterfaces.class.getName());
        List<String> interfaceNames = implementingSourceClass.getInterfaceNames();

        assertThat(interfaceNames, containsInAnyOrder(
                SimpleInterface1.class.getName(), SimpleInterface2.class.getName()));
    }

    @Test
    public void findsAbsentInterfaceNames() throws IOException, ClassNotFoundException {
        SourceClassRepository repository = makeRepository(SimpleClassWithInterfaces.class);
        SourceClass implementingSourceClass =
                repository.findClass(SimpleClassWithInterfaces.class.getName());
        List<String> interfaceNames = implementingSourceClass.getInterfaceNames();

        assertThat(interfaceNames, containsInAnyOrder(
                SimpleInterface1.class.getName(), SimpleInterface2.class.getName()));
    }
}
