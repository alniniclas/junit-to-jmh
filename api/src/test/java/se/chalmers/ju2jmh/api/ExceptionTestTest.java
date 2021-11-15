package se.chalmers.ju2jmh.api;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionTestTest {
    @Test
    public void ioExceptionSuccess() throws Throwable {
        ExceptionTest<Object> test =
                new ExceptionTest<>(o -> { throw new IOException(); }, IOException.class);

        test.accept(new Object());
    }

    @Test
    public void ioExceptionNoException() {
        ExceptionTest<Object> test = new ExceptionTest<>(o -> {}, IOException.class);

        assertThrows(AssertionError.class, () -> test.accept(new Object()));
    }

    @Test
    public void ioExceptionWrongException() {
        ExceptionTest<Object> test = new ExceptionTest<>(
                o -> { throw new ClassNotFoundException(); }, IOException.class);

        assertThrows(ClassNotFoundException.class, () -> test.accept(new Object()));
    }

    @Test
    public void ioExceptionSubclass() throws Throwable {
        ExceptionTest<Object> test =
                new ExceptionTest<>(o -> { throw new FileNotFoundException(); }, IOException.class);

        test.accept(new Object());
    }

    @Test
    public void ioExceptionSuperclass() {
        ExceptionTest<Object> test =
                new ExceptionTest<>(o -> { throw new Exception(); }, IOException.class);

        assertThrows(Exception.class, () -> test.accept(new Object()));
    }

    @Test
    public void exceptionSuccess() throws Throwable {
        ExceptionTest<Object> test =
                new ExceptionTest<>(o -> { throw new Exception(); }, Exception.class);

        test.accept(new Object());
    }

    @Test
    public void runtimeExceptionSuccess() throws Throwable {
        ExceptionTest<Object> test =
                new ExceptionTest<>(o -> { throw new RuntimeException(); }, RuntimeException.class);

        test.accept(new Object());
    }

    @Test
    public void throwableSuccess() throws Throwable {
        ExceptionTest<Object> test =
                new ExceptionTest<>(o -> { throw new Throwable(); }, Throwable.class);

        test.accept(new Object());
    }
}
