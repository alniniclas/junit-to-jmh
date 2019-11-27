package com.example;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

@org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
public class IgnoredTest {
    @Test
    @Ignore
    public void ignored() {
    }
}
