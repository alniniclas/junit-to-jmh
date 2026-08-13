plugins {
    java
    id("me.champeau.jmh") version "0.7.2"
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    implementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    implementation("org.junit.platform:junit-platform-launcher:1.10.1")
    implementation("junit:junit:4.13.2")
    runtimeOnly("org.junit.vintage:junit-vintage-engine:5.10.1")

    implementation("org.apache.commons:commons-lang3:3.14.0")
}

jmh {
    fork.set(1)
    warmupIterations.set(1)
    iterations.set(1)

}