plugins {
    java
    application
    jacoco
    id("info.solidsoft.pitest") version "1.15.0"
}


repositories {
    mavenLocal()
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.12"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

pitest {
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(listOf("se.chalmers.ju2jmh.*"))
    targetTests.set(listOf("se.chalmers.ju2jmh.*"))
    threads.set(4)
    outputFormats.set(listOf("HTML"))
    timestampedReports.set(false)
    verbose.set(true)
}

dependencies {
    val javaparserVersion: String by rootProject.extra
    val jUnitJupiterVersion: String by rootProject.extra

    val bcelVersion: String by rootProject.extra
    val jUnit4Version: String by rootProject.extra

    implementation(project(":api"))
    implementation("info.picocli", "picocli", "4.6.3")
    implementation("com.github.javaparser", "javaparser-core", "3.25.10")

    implementation("org.openjdk.jmh:jmh-core:1.37")

    implementation("org.apache.bcel", "bcel", bcelVersion)
    implementation("junit", "junit", jUnit4Version)
    implementation("com.google.guava", "guava", "31.1-jre")
    implementation("org.freemarker", "freemarker", "2.3.31")
    implementation("commons-io", "commons-io", "2.11.0")
    implementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", jUnitJupiterVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", jUnitJupiterVersion)
    testImplementation("org.hamcrest", "hamcrest-library", "2.2")
    testImplementation("io.github.java-diff-utils", "java-diff-utils", "4.12")
    testImplementation(project(":converter:test-input-classes"))

    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", jUnitJupiterVersion)
}

application {
    mainClass.set("se.chalmers.ju2jmh.Converter")

}



tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.required.set(true)
    }
}