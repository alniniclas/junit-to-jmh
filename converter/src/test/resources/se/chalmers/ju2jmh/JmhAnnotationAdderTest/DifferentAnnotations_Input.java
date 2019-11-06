package com.example;

import org.junit.Test;

public class DifferentAnnotations {
    public void notATest() {
    }

    @org.junit.Test
    public void junitTest() {
    }

    @Test
    public void importedAnnotationJunitTest() {
    }

    @org.junit.jupiter.api.Test
    public void junitJupiterTest() {
    }

    @com.example.otherframework.Test
    public void otherFrameworkTest() {
    }
}
