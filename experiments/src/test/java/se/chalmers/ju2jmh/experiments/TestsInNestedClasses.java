package se.chalmers.ju2jmh.experiments;

import org.junit.Test;

public class TestsInNestedClasses {
    public static class StaticNested {
        @Test
        public void staticNestedTest() {}

        public static class StaticNestedNested {
            @Test
            public void staticNestedNestedTest() {}
        }
    }

    public class Inner {
        @Test
        public void innerTest() {}

        public class InnerInner {
            @Test
            public void innerinnerTest() {}
        }
    }
}
