package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.Test;

public class ExceptionTest {
    @Test(expected = Exception.class)
    public void testException() throws Exception {
        throw new Exception();
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testException() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::testException, this.description("testException"), java.lang.Exception.class);
        }

        private ExceptionTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ExceptionTest();
        }

        @java.lang.Override
        public ExceptionTest implementation() {
            return this.implementation;
        }
    }
}