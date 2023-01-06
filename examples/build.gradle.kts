import com.google.protobuf.gradle.id

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm")
    id("com.google.protobuf") version "0.9.1"
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}
val grpc = "1.51.1"
dependencies {
    implementation(project(":lib"))

    val logback = "1.4.5"
    runtimeOnly("ch.qos.logback:logback-classic:$logback")
    runtimeOnly("ch.qos.logback:logback-core:$logback")

    implementation(platform("org.http4k:http4k-bom:4.35.2.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-client-apache")

    implementation("com.github.kstyrc:embedded-redis:0.6")
    implementation("org.redisson:redisson:3.19.1")

    runtimeOnly("io.grpc:grpc-netty:$grpc")
    implementation("io.grpc:grpc-protobuf:$grpc")
    implementation("io.grpc:grpc-stub:$grpc")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53") // necessary for Java 9+
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.12"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpc"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}
