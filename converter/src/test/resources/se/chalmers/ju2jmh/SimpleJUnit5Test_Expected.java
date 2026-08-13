package se.chalmers.ju2jmh.testinput.unittests;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

@State(Scope.Thread)
public class SimpleJUnit5Test_JU5Benchmark {

    private final Launcher launcher = LauncherFactory.create();
    private final Class<?> testClass = SimpleJUnit5Test.class;

    private void runBenchmark(String methodName) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectMethod(this.testClass, methodName))
            .build();
        this.launcher.execute(request);
    }

    @Benchmark
    public void benchmark_testJUnit5() {
        this.runBenchmark("testJUnit5");
    }
}
