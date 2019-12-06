plugins {
    java
    application
}

dependencies {
    val javaparserVersion: String by rootProject.extra
    val jUnitJupiterVersion: String by rootProject.extra
    val jmhVersion: String by rootProject.extra
    val bcelVersion: String by rootProject.extra
    val jUnit4Version: String by rootProject.extra

    implementation(project(":api"))
    implementation("info.picocli", "picocli", "4.0.4")
    implementation("com.github.javaparser", "javaparser-core", javaparserVersion)
    implementation("org.openjdk.jmh", "jmh-core", jmhVersion)
    implementation("org.apache.bcel", "bcel", bcelVersion)
    implementation("junit", "junit", jUnit4Version)
    implementation("com.google.guava", "guava", "28.1-jre")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", jUnitJupiterVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", jUnitJupiterVersion)
    testImplementation("org.hamcrest", "hamcrest-library", "2.2")
    testImplementation("io.github.java-diff-utils", "java-diff-utils", "4.5")
    testImplementation(project(":converter:test-input-classes"))

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", jUnitJupiterVersion)
}

application {
    mainClassName = "se.chalmers.ju2jmh.Converter"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}