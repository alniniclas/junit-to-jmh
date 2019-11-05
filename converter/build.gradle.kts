plugins {
    java
    application
}

dependencies {
    implementation("info.picocli", "picocli", "4.0.4")

    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.5.2")
    testImplementation("com.github.javaparser", "javaparser-core", "3.15.3")

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.5.2")
}

application {
    mainClassName = "se.chalmers.ju2jmh.Converter"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}