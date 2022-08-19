plugins {
    java
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    // Use JUnit 4.12 instead of JUnit 5, as 4 is the version we're targeting.
    val jUnit4Version: String by rootProject.extra

    testImplementation("junit", "junit", jUnit4Version)
    jmh("junit", "junit", jUnit4Version)
}
