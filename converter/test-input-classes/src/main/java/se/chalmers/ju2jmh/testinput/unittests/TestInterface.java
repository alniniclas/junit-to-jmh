package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public interface TestInterface {
    @Test
    default void interfaceTest() {}
}
