package se.chalmers.ju2jmh;

import org.junit.Test;

public record AmberRecordInput(int id, String name) {

    @Test
    public void testRecordBehavior() {
        // Test banale per vedere se il parser accetta il costrutto record
        if (id < 0) {
            throw new IllegalArgumentException("Negative ID");
        }
    }
}