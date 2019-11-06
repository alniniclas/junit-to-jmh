package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class MultipleClasses {
    @Test
    @org.openjdk.jmh.annotations.Benchmark
    public void test() {
    }

    @Before
    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp() {
    }

    @After
    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Invocation)
    public void tearDown() {
    }
}

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
class TestNoFixture {
    @Test
    @org.openjdk.jmh.annotations.Benchmark
    public void test() {
    }
}

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
class FixtureNoTests {
    public void notATest() {
    }

    @Before
    @org.openjdk.jmh.annotations.Setup(org.openjdk.jmh.annotations.Level.Invocation)
    public void setUp() {
    }

    @After
    @org.openjdk.jmh.annotations.TearDown(org.openjdk.jmh.annotations.Level.Invocation)
    public void tearDown() {
    }
}

class NoTestsNoFixture {
    public void notATest() {
    }
}