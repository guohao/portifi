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

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

dependencies {
    // Align versions of all Kotlin components
    api(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    api("org.slf4j:slf4j-api:2.0.5")

    // https://mvnrepository.com/artifact/io.netty/netty-all
    api("io.netty:netty-all:4.1.85.Final")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    api("com.fasterxml.jackson.core:jackson-databind:2.14.1")
}

testing {
    suites {
        // Configure the built-in test suite
        getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest()
        }
    }
}
