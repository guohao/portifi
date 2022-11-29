plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.4.20"
    application
}
dependencies {
    implementation(project(":lib"))
    runtimeOnly(libs.bundles.logback)
    implementation("com.charleskorn.kaml:kaml:0.49.0")
}

application {
    mainClass.set("io.github.gh.portifi.MainKt")
}
