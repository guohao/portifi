plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.7.22"
    application
}
dependencies {
    implementation(project(":lib"))
    runtimeOnly(libs.bundles.logback)
    implementation("com.charleskorn.kaml:kaml:0.49.0")
}

application {
    mainClass.set("io.github.guohao.portifi.MainKt")
}
