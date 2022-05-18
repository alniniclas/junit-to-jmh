plugins {
    java
    id("me.champeau.gradle.jmh") version "0.5.3"
}

dependencies {
    // Use JUnit 4.12 instead of JUnit 5, as 4 is the version we're targeting.
    val jUnit4Version: String by rootProject.extra
    val javaparserVersion: String by rootProject.extra

    implementation("junit", "junit", jUnit4Version)

    testImplementation("junit", "junit", jUnit4Version)

    jmh("junit", "junit", jUnit4Version)
    jmh("com.github.javaparser", "javaparser-core", javaparserVersion)
    jmh(project(":api"))
}

jmh {
    isIncludeTests = true
}

tasks.named<org.gradle.jvm.tasks.Jar>("jmhJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
