plugins {
    java
}

dependencies {
    val jUnitJupiterVersion: String by rootProject.extra
    val jUnit4Version: String by rootProject.extra

    implementation("junit", "junit", jUnit4Version)

    testImplementation("org.junit.jupiter", "junit-jupiter-api", jUnitJupiterVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", jUnitJupiterVersion)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}