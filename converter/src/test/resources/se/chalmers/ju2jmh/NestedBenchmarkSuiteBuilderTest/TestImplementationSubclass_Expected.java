package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class TestImplementationSubclass extends TestImplementation {
    @Test
    public void subclassTest() {}

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.testinput.unittests.TestImplementation._Benchmark {
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_subclassTest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::subclassTest, this.description("subclassTest"));
        }

        private TestImplementationSubclass implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TestImplementationSubclass();
        }

        @java.lang.Override
        public TestImplementationSubclass implementation() {
            return this.implementation;
        }
    }
}