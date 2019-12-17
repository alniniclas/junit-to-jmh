package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class TwoTestCases {
    @Test
    public void test1() {}

    @Test
    public void test2() {}

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test1, this.description("test1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test2, this.description("test2"));
        }

        private TwoTestCases implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TwoTestCases();
        }

        @java.lang.Override
        public TwoTestCases implementation() {
            return this.implementation;
        }
    }
}