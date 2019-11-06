package com.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultipleClasses {
    @Test
    public void test() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
}

class TestNoFixture {
    @Test
    public void test() {
    }
}

class FixtureNoTests {
    public void notATest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
}

class NoTestsNoFixture {
    public void notATest() {
    }
}