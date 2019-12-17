package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class TestImplementation extends TestAbstractClass implements TestInterface {
    @Test
    public void implementationTest() {}

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.testinput.unittests.TestAbstractClass._Benchmark {
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_implementationTest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::implementationTest, this.description("implementationTest"));
        }

        private TestImplementation implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TestImplementation();
        }

        @java.lang.Override
        public TestImplementation implementation() {
            return this.implementation;
        }
    }
}