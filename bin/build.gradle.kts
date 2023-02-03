plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.10"
    application
}
dependencies {
    implementation(project(":lib"))
    runtimeOnly(libs.bundles.logback)
    implementation("com.charleskorn.kaml:kaml:0.51.0")
}

application {
    mainClass.set("io.github.guohao.portifi.MainKt")
}
