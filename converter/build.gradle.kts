plugins {
    java
    application
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.5.2")
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", "5.5.2")
}

application {
    mainClassName = "se.chalmers.ju2jmh.Converter"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}