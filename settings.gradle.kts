rootProject.name = "coding-assistant-ai"

// Rejestracja modułów
include("shared")
include("backend")
include("plugin")

// Zarządzanie pluginami w jednym miejscu (wersje)
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev") }
        maven { url = uri("https://maven.pkg.jetbrains.space/intellij/p/intellij/intellij-platform-gradle-plugin") }
    }
}