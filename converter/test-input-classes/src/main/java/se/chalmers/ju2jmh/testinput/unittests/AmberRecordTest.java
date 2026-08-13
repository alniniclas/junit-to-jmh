package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.jupiter.api.Test;

/**
 * Test case validation for Java 17 record features.
 */
record User(String name, int id) {}

public class AmberRecordTest {

    @Test
    public void testRecordFeature() {
        User user = new User("TestUser", 101);
        if (!user.name().equals("TestUser")) {
            throw new AssertionError("Record implementation failed");
        }
    }
}
