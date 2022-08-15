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

tasks.register<Copy>("copySourcesToResources") {
    val mainSourceSet = sourceSets.main.get()
    from("src/main/java")
    into("build/resources/main")
    include("**/*.java")
}

tasks.named("processResources") {
    dependsOn("copySourcesToResources")
}
