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
    }
}