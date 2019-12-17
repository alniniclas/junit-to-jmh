package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class ClassWithNestedTestSubclasses {
    public static class NestedSubclass extends ClassWithNestedTests.Nested {
        @Test
        public void nestedSubclassTest() {}

        public static class NestedNestedSubclass extends ClassWithNestedTests.Nested.NestedNested {
            @Test
            public void nestedNestedSubclassTest() {}

            @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
            public static class _Benchmark extends se.chalmers.ju2jmh.testinput.unittests.ClassWithNestedTests.Nested.NestedNested._Benchmark {
                @org.openjdk.jmh.annotations.Benchmark
                public void benchmark_nestedNestedSubclassTest() throws java.lang.Throwable {
                    this.createImplementation();
                    this.runBenchmark(this.implementation()::nestedNestedSubclassTest, this.description("nestedNestedSubclassTest"));
                }

                private NestedNestedSubclass implementation;

                @java.lang.Override
                public void createImplementation() throws java.lang.Throwable {
                    this.implementation = new NestedNestedSubclass();
                }

                @java.lang.Override
                public NestedNestedSubclass implementation() {
                    return this.implementation;
                }
            }
        }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
        public static class _Benchmark extends se.chalmers.ju2jmh.testinput.unittests.ClassWithNestedTests.Nested._Benchmark {
            @org.openjdk.jmh.annotations.Benchmark
            public void benchmark_nestedSubclassTest() throws java.lang.Throwable {
                this.createImplementation();
                this.runBenchmark(this.implementation()::nestedSubclassTest, this.description("nestedSubclassTest"));
            }

            private NestedSubclass implementation;

            @java.lang.Override
            public void createImplementation() throws java.lang.Throwable {
                this.implementation = new NestedSubclass();
            }

            @java.lang.Override
            public NestedSubclass implementation() {
                return this.implementation;
            }
        }
    }
}
