package com.hrm.testutil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class SimpleTestRunner {
    private SimpleTestRunner() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("No test classes provided.");
            System.exit(1);
        }

        int passed = 0;
        int failed = 0;

        for (String className : args) {
            Class<?> testClass = Class.forName(className);
            List<Method> beforeEachMethods = findAnnotatedMethods(testClass, BeforeEach.class);
            List<Method> afterEachMethods = findAnnotatedMethods(testClass, AfterEach.class);
            List<Method> testMethods = findAnnotatedMethods(testClass, Test.class);

            for (Method testMethod : testMethods) {
                Constructor<?> constructor = testClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object instance = constructor.newInstance();

                try {
                    invokeAll(beforeEachMethods, instance);
                    testMethod.setAccessible(true);
                    testMethod.invoke(instance);
                    passed++;
                    System.out.println("PASS " + className + "#" + testMethod.getName());
                } catch (InvocationTargetException ex) {
                    failed++;
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    System.out.println("FAIL " + className + "#" + testMethod.getName()
                            + " -> " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
                } finally {
                    invokeAll(afterEachMethods, instance);
                }
            }
        }

        System.out.println("SUMMARY passed=" + passed + " failed=" + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static List<Method> findAnnotatedMethods(Class<?> type, Class<?> annotationType) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent((Class) annotationType))
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }

    private static void invokeAll(List<Method> methods, Object instance) throws Exception {
        for (Method method : methods) {
            method.setAccessible(true);
            method.invoke(instance);
        }
    }
}
