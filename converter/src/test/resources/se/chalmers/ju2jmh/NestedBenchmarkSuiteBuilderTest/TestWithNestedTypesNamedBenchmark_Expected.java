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

                @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
                public static class _Benchmark_0 extends se.chalmers.ju2jmh.testinput.unittests.TestWithNestedTypesNamedBenchmark._Benchmark_4 {
                    @org.openjdk.jmh.annotations.Benchmark
                    public void benchmark_test4() throws java.lang.Throwable {
                        this.createImplementation();
                        this.runBenchmark(this.implementation()::test4, this.description("test4"));
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

            public static class _Benchmark_0 {}

            @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
            public static class _Benchmark_1 extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
                @org.openjdk.jmh.annotations.Benchmark
                public void benchmark_test3() throws java.lang.Throwable {
                    this.createImplementation();
                    this.runBenchmark(this.implementation()::test3, this.description("test3"));
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

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
        public static class _Benchmark_0 extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
            @org.openjdk.jmh.annotations.Benchmark
            public void benchmark_test2() throws java.lang.Throwable {
                this.createImplementation();
                this.runBenchmark(this.implementation()::test2, this.description("test2"));
            }

            private _Benchmark implementation;

            @java.lang.Override
            public void createImplementation() throws java.lang.Throwable {
                this.implementation = new _Benchmark();
            }

            @java.lang.Override
            public _Benchmark implementation() {
                return this.implementation;
            }
        }
    }

    public class _Benchmark_0 {}
    public interface _Benchmark_1 {}
    public  @interface _Benchmark_2 {}
    public enum _Benchmark_3 {}

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark_4 extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test1, this.description("test1"));
        }

        private TestWithNestedTypesNamedBenchmark implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TestWithNestedTypesNamedBenchmark();
        }

        @java.lang.Override
        public TestWithNestedTypesNamedBenchmark implementation() {
            return this.implementation;
        }
    }
}
