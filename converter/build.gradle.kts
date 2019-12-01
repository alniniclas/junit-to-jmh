plugins {
    java
    application
}

dependencies {
    val javaparserVersion: String by rootProject.extra
    val jUnitJupiterVersion: String by rootProject.extra
    val jmhVersion: String by rootProject.extra

    implementation("info.picocli", "picocli", "4.0.4")
    implementation("com.github.javaparser", "javaparser-core", javaparserVersion)
    implementation("org.openjdk.jmh", "jmh-core", jmhVersion)
    implementation(project(":api"))

    testImplementation("org.junit.jupiter", "junit-jupiter-api", jUnitJupiterVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", jUnitJupiterVersion)
    testImplementation("com.github.javaparser", "javaparser-core", javaparserVersion)

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", jUnitJupiterVersion)
}

application {
    mainClassName = "se.chalmers.ju2jmh.Converter"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}