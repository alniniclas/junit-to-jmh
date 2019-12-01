package se.chalmers.ju2jmh.experiments;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JUnit4TestInvoker {
    private JUnit4TestInvoker() {
        throw new AssertionError("Should not be instantiated");
    }

    private static <T> Statement applyTestRules(
            T testInstance, Statement testStatement, Description testDescription)
            throws IllegalAccessException, InvocationTargetException {
        Class<?> testClass = testInstance.getClass();
        for (Method method : testClass.getMethods()) {
            if (!method.isAnnotationPresent(Rule.class)
                    || !TestRule.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }
            testStatement = ((TestRule) method.invoke(testInstance))
                    .apply(testStatement, testDescription);
        }
        for (Field field : testClass.getFields()) {
            if (!field.isAnnotationPresent(Rule.class)
                    || !TestRule.class.isAssignableFrom(field.getType())) {
                continue;
            }
            testStatement = ((TestRule) field.get(testInstance))
                    .apply(testStatement, testDescription);
        }
        return testStatement;
    }

    private static <T> void invokeBefore(T testInstance)
            throws IllegalAccessException, InvocationTargetException {
        for (Method method : testInstance.getClass().getMethods()) {
            if (method.isAnnotationPresent(Before.class)) {
                method.invoke(testInstance);
            }
        }
    }

    private static <T> void invokeAfter(T testInstance)
            throws IllegalAccessException, InvocationTargetException {
        for (Method method : testInstance.getClass().getMethods()) {
            if (method.isAnnotationPresent(After.class)) {
                method.invoke(testInstance);
            }
        }
    }

    public static <T> void invoke(Class<T> testClass, String methodName) throws Throwable {
        T testInstance = testClass.getConstructor().newInstance();
        Statement testStatement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                invokeBefore(testInstance);
                testClass.getMethod(methodName).invoke(testInstance);
                invokeAfter(testInstance);
            }
        };
        Description testDescription = Description.createTestDescription(testClass, methodName);
        applyTestRules(testInstance, testStatement, testDescription).evaluate();
    }
}
