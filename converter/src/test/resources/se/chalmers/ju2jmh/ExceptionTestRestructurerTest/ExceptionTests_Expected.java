package com.example;

import org.junit.Test;

public class ExceptionTests {
    @Test
    public void noExceptionExpected() {
    }

    @Test
    public void exceptionExpected() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(Exception.class, () -> {
            throw new Exception();
        });
    }

    @Test
    public void classNotFoundExceptionExpected() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(ClassNotFoundException.class, () -> {
            throw new ClassNotFoundException();
        });
    }

    @Test
    public void classNotFoundExceptionSuperclassExpected() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(Exception.class, () -> {
            throw new ClassNotFoundException();
        });
    }

    @Test
    public void runtimeExceptionExpected() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException();
        });
    }

    @Test
    public void errorExpected() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(Error.class, () -> {
            throw new Error();
        });
    }

    @Test
    public void throwableExpected() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(Throwable.class, () -> {
            throw new Throwable();
        });
    }

    @Test
    public void assertionErrorExpected() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(AssertionError.class, () -> {
            throw new AssertionError();
        });
    }

    @Test
    public void fullyQualifiedExceptionExpected() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(java.lang.Exception.class, () -> {
            throw new java.lang.Exception();
        });
    }

    @Test(timeout = 1000L)
    public void exceptionExpectedWithTimeout() {
        se.chalmers.ju2jmh.api.ExceptionTest.assertThrows(Exception.class, () -> {
            throw new Exception();
        });
    }

    @Test(timeout = 1000L)
    public void noExceptionExpectedWithTimeout() {
    }
}
