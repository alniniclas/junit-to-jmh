package se.chalmers.ju2jmh.experiments;

import org.junit.Test;

public interface TestInterface {
    @Test
    default void interfaceTest() {}
}
