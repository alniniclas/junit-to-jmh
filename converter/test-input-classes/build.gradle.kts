plugins {
    java
}

dependencies {
    val jUnit4Version: String by rootProject.extra

    compileOnly("junit", "junit", jUnit4Version)
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
