import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    kotlin("plugin.serialization") version "1.5.21"
}

group = "com.kotakotik"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.eclipse.org/content/groups/releases")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("org.javacord:javacord:3.3.2");
    implementation(kotlin("script-runtime"))
    implementation("org.reflections:reflections:0.9.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    api("com.github.TheRandomLabs:CurseAPI:master-SNAPSHOT")
    runtimeOnly("com.google.code.gson:gson:2.8.7")
    implementation("org.eclipse.mylyn.github:org.eclipse.egit.github.core:5.9.0.202009080501-r")
    implementation(kotlin("reflect"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}


tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "handy.Handy"
        )
    }
}