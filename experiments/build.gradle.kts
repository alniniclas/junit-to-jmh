plugins {
    java
    id("me.champeau.gradle.jmh") version "0.5.0"
}

dependencies {
    // Use JUnit 4.12 instead of JUnit 5, as 4 is the version we're targeting.
    val jUnit4Version: String by rootProject.extra

    implementation("junit", "junit", jUnit4Version)

    testImplementation("junit", "junit", jUnit4Version)

    jmh("junit", "junit", jUnit4Version)
    jmh(project(":api"))
}

jmh {
    isIncludeTests = true
}