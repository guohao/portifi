plugins {
    kotlin("jvm") version "1.7.22" apply false
}
group = "io.github.gh"
version = "0.0.2-SNAPSHOT"

allprojects {
    repositories {
        // Use Maven Central for resolving dependencies.
        mavenLocal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}
