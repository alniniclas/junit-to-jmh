plugins {
    java
    application
}

dependencies {
    val javaparserVersion = "3.15.3"
    val junitVersion = "5.5.2"

    implementation("info.picocli", "picocli", "4.0.4")
    implementation("com.github.javaparser", "javaparser-core", javaparserVersion)
    implementation("org.openjdk.jmh", "jmh-core", "1.22")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion)
    testImplementation("com.github.javaparser", "javaparser-core", javaparserVersion)

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

application {
    mainClassName = "se.chalmers.ju2jmh.Converter"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}