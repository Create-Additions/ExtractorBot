import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.kotakotik"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("org.javacord:javacord:3.3.2");
    implementation("com.google.code.gson:gson:2.8.7")
    implementation(kotlin("script-runtime"))
    implementation("org.reflections:reflections:0.9.12")
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