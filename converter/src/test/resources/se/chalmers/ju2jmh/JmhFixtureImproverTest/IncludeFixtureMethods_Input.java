package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class IncludeFixtureMethods {
    public void notATest() {
    }

    @Test
    @org.openjdk.jmh.annotations.Benchmark
    public void test1() {
    }

    @Test
    @org.openjdk.jmh.annotations.Benchmark
    public void test2() {
        notATest();
    }

    @Before
    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp1() {
    }

    @Before
    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp2() {
    }

    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Iteration)
    public void setUpNonInvocation() {
    }

    @After
    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Invocation)
    public void tearDown1() {
    }

    @After
    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Invocation)
    public void tearDown2() {
    }

    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Iteration)
    public void tearDownNonInvocation() {
    }
}
