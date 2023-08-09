/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/7.4.2/userguide/multi_project_builds.html
 * This project uses @Incubating APIs which are subject to change.
 */

rootProject.name = "portifi"
include(":lib")
include(":bin")
include(":examples")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val logback = "1.4.11"
            library("logback-classic", "ch.qos.logback:logback-classic:$logback")
            library("logback-core", "ch.qos.logback:logback-core:$logback")
            bundle("logback", listOf("logback-classic", "logback-core"))
        }
    }
}
