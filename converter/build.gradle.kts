plugins {
    java
    application
}

dependencies {
    val javaparserVersion = "3.15.3"

    implementation("info.picocli", "picocli", "4.0.4")
    implementation("com.github.javaparser", "javaparser-core", javaparserVersion)
    implementation("org.openjdk.jmh", "jmh-core", "1.22")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.5.2")
    testImplementation("com.github.javaparser", "javaparser-core", javaparserVersion)

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.5.2")
}

application {
    mainClassName = "se.chalmers.ju2jmh.Converter"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}