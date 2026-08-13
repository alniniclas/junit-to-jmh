package se.chalmers.ju2jmh.testinput.unittests.junit5;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DisabledTest {

    @Test
    public void activeTest() {

    }

    @Test
    @Disabled("not supposed to be a benchmark")
    public void skippedTest() {
        throw new RuntimeException("Not supposed to run");
    }



}