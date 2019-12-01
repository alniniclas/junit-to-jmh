plugins {
    java
}

dependencies {
    val jUnitJupiterVersion: String by rootProject.extra

    testImplementation("org.junit.jupiter", "junit-jupiter-api", jUnitJupiterVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", jUnitJupiterVersion)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}