/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.4.2/userguide/building_java_projects.html
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.7.22"
    `java-library`
    `maven-publish`
}

dependencies {
    api(platform("org.jetbrains.kotlin:kotlin-bom"))

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.slf4j:slf4j-api:2.0.6")
    api("io.netty:netty-all:4.1.86.Final")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
}

testing {
    suites {
        // Configure the built-in test suite
        getting(JvmTestSuite::class) {
            // Use KotlinTest test framework
            useKotlinTest()
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/guohao/portifi")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "io.github.guohao"
            version = "0.0.2-SNAPSHOT"
            artifactId = "portifi"
        }
    }
}
