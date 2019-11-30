plugins {
    java
    id("me.champeau.gradle.jmh") version "0.5.0"
}

dependencies {
    // Use JUnit 4.12 instead of JUnit 5, as 4 is the version we're targeting.
    val junitVersion = "4.12"
    testImplementation("junit", "junit", junitVersion)
    jmh("junit", "junit", junitVersion)
}

jmh {
    // Very short iteration time, as we're only interested in seeing whether the benchmarks are able
    // to execute without errors rather than measuring the actual performance characteristics of the
    // example code.
    timeOnIteration = "10 ms"
    warmup = "10 ms"
}
