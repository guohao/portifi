plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.0"
    application
}
dependencies {
    implementation(project(":lib"))
    runtimeOnly(libs.bundles.logback)
    implementation("com.charleskorn.kaml:kaml:0.54.0")
}

application {
    mainClass.set("io.github.guohao.portifi.MainKt")
}
