package com.example;

import org.junit.Test;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class DifferentAnnotations {
    public void notATest() {
    }

    @org.junit.Test
    @org.openjdk.jmh.annotations.Benchmark
    public void junitTest() {
    }

    @Test
    @org.openjdk.jmh.annotations.Benchmark
    public void importedAnnotationJunitTest() {
    }

    @org.junit.jupiter.api.Test
    @org.openjdk.jmh.annotations.Benchmark
    public void junitJupiterTest() {
    }

    @com.example.otherframework.Test
    @org.openjdk.jmh.annotations.Benchmark
    public void otherFrameworkTest() {
    }
}
