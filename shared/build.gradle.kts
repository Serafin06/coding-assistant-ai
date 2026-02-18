plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0") // Zaktualizowana wersja pod Kotlin 2.3
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.0") // Nowsze korutyny
}