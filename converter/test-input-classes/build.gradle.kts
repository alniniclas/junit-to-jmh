plugins {
    java
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
