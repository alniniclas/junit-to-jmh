package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class FixtureMethods {
    public void notATest() {
    }

    @Test
    @org.openjdk.jmh.annotations.Benchmark
    public void test() {
    }

    @Before
    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp1() {
    }

    @Before
    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp2() {
    }

    @After
    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Invocation)
    public void tearDown1() {
    }

    @After
    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Invocation)
    public void tearDown2() {
    }
}
