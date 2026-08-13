package se.chalmers.ju2jmh.testinput.fixtures;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

@State(Scope.Thread)
public class BENCHMARK_TEMPLATE {

    private final Launcher launcher = LauncherFactory.create();

    private void runBenchmark(String className, String methodName) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectMethod(className, methodName))
                .build();

        this.launcher.execute(request);
    }

}