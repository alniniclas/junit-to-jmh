package se.chalmers.ju2jmh.junit5;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DisabledTest {

    @Test
    public void activeTest() {
    }

    @Test
    @Disabled("test disabled for demonstration")
    public void skippedTest() {
        throw new RuntimeException("test not supposed to run");
    }

    @Test
    public void activeTestasd() {
    }

}