
plugins {
    // Używamy wersji zgodnych z Twoim środowiskiem "2026"
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
    id("io.ktor.plugin") version "3.4.0" apply false

    // NOWY plugin do IntelliJ (wymagany dla Gradle 9.0 i IDEA 2025+)
    id("org.jetbrains.intellij.platform") version "2.11.0" apply false
}

allprojects {
    group = "pl.rafapp.assistant"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }
}
