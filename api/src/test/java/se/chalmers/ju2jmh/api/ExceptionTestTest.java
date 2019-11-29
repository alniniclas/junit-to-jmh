package se.chalmers.ju2jmh.api;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionTestTest {
    @Test
    public void ioExceptionSuccess() {
        ExceptionTest test = () -> { throw new IOException(); };
        ExceptionTest.assertThrows(IOException.class, test);
    }

    @Test
    public void ioExceptionNoException() {
        ExceptionTest test = () -> {};

        assertThrows(AssertionError.class,
                () -> ExceptionTest.assertThrows(IOException.class, test));
    }

    @Test
    public void ioExceptionWrongException() {
        ExceptionTest test = () -> { throw new ClassNotFoundException(); };

        assertThrows(AssertionError.class,
                () -> ExceptionTest.assertThrows(IOException.class, test));
    }

    @Test
    public void ioExceptionSubclass() {
        ExceptionTest test = () -> { throw new FileNotFoundException(); };

        ExceptionTest.assertThrows(IOException.class, test);
    }

    @Test
    public void ioExceptionSuperclass() {
        ExceptionTest test = () -> { throw new Exception(); };

        assertThrows(AssertionError.class,
                () -> ExceptionTest.assertThrows(IOException.class, test));
    }

    @Test
    public void exceptionSuccess() {
        ExceptionTest test = () -> { throw new Exception(); };
        ExceptionTest.assertThrows(Exception.class, test);
    }

    @Test
    public void runtimeExceptionSuccess() {
        ExceptionTest test = () -> { throw new RuntimeException(); };
        ExceptionTest.assertThrows(RuntimeException.class, test);
    }

    @Test
    public void throwableSuccess() {
        ExceptionTest test = () -> { throw new Throwable(); };
        ExceptionTest.assertThrows(Throwable.class, test);
    }
}
