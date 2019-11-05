package com.example;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleTest {
    @Test
    @org.openjdk.jmh.annotations.Benchmark()
    public void test1() {
    }

    @Test
    @org.openjdk.jmh.annotations.Benchmark()
    public void test2() {
        assertEquals(4, 2 + 2);
    }

    @Test
    @org.openjdk.jmh.annotations.Benchmark()
    public void test3() {
        assertFalse(1 + 1 == 3);
    }
}
