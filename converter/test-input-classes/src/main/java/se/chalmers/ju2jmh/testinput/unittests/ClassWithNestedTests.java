package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class ClassWithNestedTests {
    public static class Nested {
        @Test
        public void nestedTest() {}

        public static class NestedNested {
            @Test
            public void nestedNestedTest() {}
        }
    }
}
