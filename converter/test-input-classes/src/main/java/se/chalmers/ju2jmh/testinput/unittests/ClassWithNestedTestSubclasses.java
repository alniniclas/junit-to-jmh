package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class ClassWithNestedTestSubclasses {
    public static class NestedSubclass extends ClassWithNestedTests.Nested {
        @Test
        public void nestedSubclassTest() {}

        public static class NestedNestedSubclass extends ClassWithNestedTests.Nested.NestedNested {
            @Test
            public void nestedNestedSubclassTest() {}
        }
    }
}
