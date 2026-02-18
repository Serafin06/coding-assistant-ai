plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
}

dependencies {
    implementation(project(":shared"))

    // Ktor 3.4.0
    implementation("io.ktor:ktor-server-core:3.4.0")
    implementation("io.ktor:ktor-server-netty:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")

    implementation("ch.qos.logback:logback-classic:1.5.15")
}