package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public abstract class TestAbstractClass {
    @Test
    public void abstractClassTest() {}

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static abstract class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_abstractClassTest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::abstractClassTest, this.description("abstractClassTest"));
        }

        @java.lang.Override
        public abstract void createImplementation() throws java.lang.Throwable;

        @java.lang.Override
        public abstract TestAbstractClass implementation();
    }
}
