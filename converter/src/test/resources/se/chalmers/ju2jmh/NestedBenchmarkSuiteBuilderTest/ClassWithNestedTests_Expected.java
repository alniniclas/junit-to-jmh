package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class ClassWithNestedTests {
    public static class Nested {
        @Test
        public void nestedTest() {}

        public static class NestedNested {
            @Test
            public void nestedNestedTest() {}

            @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
            public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
                @org.openjdk.jmh.annotations.Benchmark
                public void benchmark_nestedNestedTest() throws java.lang.Throwable {
                    this.createImplementation();
                    this.runBenchmark(this.implementation()::nestedNestedTest, this.description("nestedNestedTest"));
                }

                private NestedNested implementation;

                @java.lang.Override
                public void createImplementation() throws java.lang.Throwable {
                    this.implementation = new NestedNested();
                }

                @java.lang.Override
                public NestedNested implementation() {
                    return this.implementation;
                }
            }
        }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
        public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
            @org.openjdk.jmh.annotations.Benchmark
            public void benchmark_nestedTest() throws java.lang.Throwable {
                this.createImplementation();
                this.runBenchmark(this.implementation()::nestedTest, this.description("nestedTest"));
            }

            private Nested implementation;

            @java.lang.Override
            public void createImplementation() throws java.lang.Throwable {
                this.implementation = new Nested();
            }

            @java.lang.Override
            public Nested implementation() {
                return this.implementation;
            }
        }
    }
}
