plugins {
    java
}

dependencies {
    implementation(project(":converter"))
    implementation("com.github.javaparser:javaparser-core:3.25.5")

    implementation("junit:junit:4.13.2")
    runtimeOnly("org.junit.vintage:junit-vintage-engine:5.10.1")

    implementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    implementation("org.junit.platform:junit-platform-launcher:1.10.1")
    implementation("org.junit.platform:junit-platform-engine:1.10.1")
    implementation("org.hamcrest:hamcrest:2.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}