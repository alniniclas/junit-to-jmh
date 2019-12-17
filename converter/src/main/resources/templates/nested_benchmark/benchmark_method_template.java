@org.openjdk.jmh.annotations.Benchmark
public void BENCHMARK_METHOD_NAME() throws java.lang.Throwable {
    this.createImplementation();
    this.runBenchmark(this.implementation()::TEST_METHOD_NAME, this.description("TEST_METHOD_NAME"));
}
