package com.example;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleTest {
    @Test
    public void test1() {
    }

    @Test
    public void test2() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test3() {
        assertFalse(1 + 1 == 3);
    }
}
