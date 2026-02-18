plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("pl.rafapp.assistant.ApplicationKt")
}

dependencies {
    implementation(project(":shared"))

    implementation("io.ktor:ktor-server-core:3.4.0")
    implementation("io.ktor:ktor-server-netty:3.4.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
    implementation("io.ktor:ktor-server-cors:3.4.0")

    implementation("dev.langchain4j:langchain4j:1.0.0-beta2")
    implementation("dev.langchain4j:langchain4j-ollama:1.0.0-beta2")

    implementation("com.typesafe:config:1.4.3")
    implementation("ch.qos.logback:logback-classic:1.5.13")
}