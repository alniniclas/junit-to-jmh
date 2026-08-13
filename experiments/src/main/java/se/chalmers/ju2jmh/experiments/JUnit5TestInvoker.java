package se.chalmers.ju2jmh.experiments;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.engine.discovery.DiscoverySelectors;

public class JUnit5TestInvoker {

    public static void invoke(Class<?> testClass, String methodName) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectMethod(testClass, methodName))
                .build();

        Launcher launcher = LauncherFactory.create();
        launcher.execute(request);
    }
}