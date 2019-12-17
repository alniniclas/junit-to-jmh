package se.chalmers.ju2jmh.testinput.unittests;

import org.junit.After;
import org.junit.Before;

public class ClassWithOnlyFixtureMethods {
    @Before
    public void before() {}

    @After
    public void after() {}

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {
        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().before();
        }

        @java.lang.Override
        public void after() throws java.lang.Throwable {
            this.implementation().after();
            super.after();
        }

        private ClassWithOnlyFixtureMethods implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ClassWithOnlyFixtureMethods();
        }

        @java.lang.Override
        public ClassWithOnlyFixtureMethods implementation() {
            return this.implementation;
        }
    }
}