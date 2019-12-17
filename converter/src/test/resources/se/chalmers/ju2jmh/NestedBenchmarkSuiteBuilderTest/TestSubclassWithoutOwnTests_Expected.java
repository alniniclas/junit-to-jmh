package se.chalmers.ju2jmh.testinput.unittests;

public class TestSubclassWithoutOwnTests extends TestAbstractClass {

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.testinput.unittests.TestAbstractClass._Benchmark {
        private TestSubclassWithoutOwnTests implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new TestSubclassWithoutOwnTests();
        }

        @java.lang.Override
        public TestSubclassWithoutOwnTests implementation() {
            return this.implementation;
        }
    }
}