package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class SimpleUnitTest {
    @Test
    public void test() {}

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test, this.description("test"));
        }

        private SimpleUnitTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SimpleUnitTest();
        }

        @java.lang.Override
        public SimpleUnitTest implementation() {
            return this.implementation;
        }
    }
}