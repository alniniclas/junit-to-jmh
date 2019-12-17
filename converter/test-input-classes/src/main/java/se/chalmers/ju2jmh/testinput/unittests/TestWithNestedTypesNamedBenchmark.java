package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class TestWithNestedTypesNamedBenchmark {
    @Test
    public void test1() {}

    public static class _Benchmark {
        @Test
        public void test2() {}

        public static class Nested {
            @Test
            public void test3() {}

            public static class NestedNested extends TestWithNestedTypesNamedBenchmark {
                @Test
                public void test4() {}
            }

            public static class _Benchmark_0 {}
        }
    }

    public class _Benchmark_0 {}
    public interface _Benchmark_1 {}
    public @interface _Benchmark_2 {}
    public enum _Benchmark_3 {}
}
