package com.example;

import org.junit.Test;

public class ExceptionTests {
    @Test
    public void noExceptionExpected() {
    }

    @Test(expected = Exception.class)
    public void exceptionExpected() throws Exception {
        throw new Exception();
    }

    @Test(expected = ClassNotFoundException.class)
    public void classNotFoundExceptionExpected() throws ClassNotFoundException {
        throw new ClassNotFoundException();
    }

    @Test(expected = Exception.class)
    public void classNotFoundExceptionSuperclassExpected() throws ClassNotFoundException {
        throw new ClassNotFoundException();
    }

    @Test(expected = RuntimeException.class)
    public void runtimeExceptionExpected() {
        throw new RuntimeException();
    }

    @Test(expected = Error.class)
    public void errorExpected() {
        throw new Error();
    }

    @Test(expected = Throwable.class)
    public void throwableExpected() throws Throwable {
        throw new Throwable();
    }

    @Test(expected = AssertionError.class)
    public void assertionErrorExpected() {
        throw new AssertionError();
    }

    @Test(expected = java.lang.Exception.class)
    public void fullyQualifiedExceptionExpected() throws java.lang.Exception {
        throw new java.lang.Exception();
    }

    @Test(expected = Exception.class, timeout = 1000L)
    public void exceptionExpectedWithTimeout() throws Exception {
        throw new Exception();
    }

    @Test(timeout = 1000L)
    public void noExceptionExpectedWithTimeout() {
    }
}
