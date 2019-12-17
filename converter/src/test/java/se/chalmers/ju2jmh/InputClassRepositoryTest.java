package se.chalmers.ju2jmh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.chalmers.ju2jmh.testinput.NestedClasses;
import se.chalmers.ju2jmh.testinput.SimpleClass;
import se.chalmers.ju2jmh.testinput.SimpleClassWithInterfaces;
import se.chalmers.ju2jmh.testinput.SimpleInterface1;
import se.chalmers.ju2jmh.testinput.SimpleInterface2;
import se.chalmers.ju2jmh.testinput.SimpleSubclass;
import se.chalmers.ju2jmh.testinput.SimpleSuperclass;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

public class InputClassRepositoryTest {
    private InputClassDirectory inputClassDirectory;

    @BeforeEach
    public void setUpInputClassDir(@TempDir Path tempDir) {
        inputClassDirectory = new InputClassDirectory(tempDir);
    }

    private InputClassRepository makeRepository(Class<?>... classes)
            throws IOException, ClassNotFoundException {
        for (Class<?> clazz : classes) {
            inputClassDirectory.add(clazz);
        }
        return new InputClassRepository(inputClassDirectory.sourcesDirectory(),
                inputClassDirectory.bytecodeDirectory());
    }

    @Test
    public void findsClassWithBytecodeAndSources() throws IOException, ClassNotFoundException {
        InputClassRepository repository = makeRepository();
        inputClassDirectory.add(SimpleClass.class);
        InputClass simpleClass = repository.findClass(SimpleClass.class.getName());
        assertEquals(SimpleClass.class.getName(), simpleClass.getName());
    }

    @Test
    public void failsToFindAbsentClass() throws IOException, ClassNotFoundException {
        InputClassRepository repository = makeRepository();
        assertThrows(ClassNotFoundException.class,
                () -> repository.findClass(SimpleClass.class.getName()));
    }

    @Test
    public void failsToFindClassWithAbsentSource() throws IOException, ClassNotFoundException {
        InputClassRepository repository = makeRepository();
        inputClassDirectory.addBytecode(SimpleClass.class);
        assertThrows(ClassNotFoundException.class,
                () -> repository.findClass(SimpleClass.class.getName()));
    }

    @Test
    public void failsToFindClassWithAbsentBytecode() throws IOException, ClassNotFoundException {
        InputClassRepository repository = makeRepository();
        inputClassDirectory.addSource(SimpleClass.class);
        assertThrows(ClassNotFoundException.class,
                () -> repository.findClass(SimpleClass.class.getName()));
    }

    @Test
    public void returnsSameObject() throws IOException, ClassNotFoundException {
        InputClassRepository repository = makeRepository(SimpleClass.class);
        InputClass first = repository.findClass(SimpleClass.class.getName());
        InputClass second = repository.findClass(SimpleClass.class.getName());
        assertSame(first, second);
    }

    @Test
    public void findsPresentSuperclassName() throws IOException, ClassNotFoundException {
        InputClassRepository repository =
                makeRepository(SimpleSubclass.class, SimpleSuperclass.class);
        InputClass subclass = repository.findClass(SimpleSubclass.class.getName());
        String superclassName = subclass.getSuperclassName();
        assertEquals(SimpleSuperclass.class.getName(), superclassName);
    }

    @Test
    public void findsAbsentSuperclassName() throws IOException, ClassNotFoundException {
        InputClassRepository repository = makeRepository(SimpleSubclass.class);
        InputClass subclass = repository.findClass(SimpleSubclass.class.getName());
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
        InputClassRepository repository = makeRepository(classes);
        for (Class<?> clazz : classes) {
            InputClass inputClass = repository.findClass(clazz.getName());
            assertEquals(clazz.getName(), inputClass.getName());
            String sourceSimpleName =
                    inputClass.getSource().getName().asString();
            assertEquals(clazz.getSimpleName(), sourceSimpleName);
        }
    }

    @Test
    public void findsPresentInterfaceNames() throws IOException, ClassNotFoundException {
        InputClassRepository repository = makeRepository(
                SimpleClassWithInterfaces.class, SimpleInterface1.class, SimpleInterface2.class);
        InputClass implementingInputClass =
                repository.findClass(SimpleClassWithInterfaces.class.getName());
        List<String> interfaceNames = implementingInputClass.getInterfaceNames();

        assertThat(interfaceNames, containsInAnyOrder(
                SimpleInterface1.class.getName(), SimpleInterface2.class.getName()));
    }

    @Test
    public void findsAbsentInterfaceNames() throws IOException, ClassNotFoundException {
        InputClassRepository repository = makeRepository(SimpleClassWithInterfaces.class);
        InputClass implementingInputClass =
                repository.findClass(SimpleClassWithInterfaces.class.getName());
        List<String> interfaceNames = implementingInputClass.getInterfaceNames();

        assertThat(interfaceNames, containsInAnyOrder(
                SimpleInterface1.class.getName(), SimpleInterface2.class.getName()));
    }
}
