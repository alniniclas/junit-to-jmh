plugins {
    java
}

dependencies {
    val junitVersion = "5.5.2"
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}