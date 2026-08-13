package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class AmberGranularityTest {

    // 1. Feature Java 17 (Record)
    public record Person(String name, int age) {}

    // conversion test for Java 17 record features, should be converted to a normal class with getters and constructor
    @Test
    public void testAmberFeature() {
        Person p = new Person("Benito", 24);
        System.out.println(p);
    }

    // ignored test
    @Test
    public void testToSkip() {
        System.out.println("Test to be skipped");
    }
}
