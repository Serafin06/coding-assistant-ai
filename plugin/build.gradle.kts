plugins {
    kotlin("jvm")
    // Upewnij się, że w root build.gradle.kts masz wersję 2.11.0 dla tego pluginu
    id("org.jetbrains.intellij.platform")
    kotlin("plugin.serialization") // Dodaj to, skoro używasz kotlinx-serialization
}

// 1. WYMUSZENIE JAVA 21 - to rozwiąże błąd "targetCompatibility=24"
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":shared"))

    // Klient do Twojego backendu (używamy OkHttp zgodnie z Twoim wyborem)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    intellijPlatform {
        intellijIdeaCommunity("2025.2.3")
        instrumentationTools()
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "AI Coding Assistant"
        version = "0.0.1"

        ideaVersion {
            // Ponieważ używasz IntelliJ 2025.2, te wartości są poprawne
            sinceBuild.set("252")
            untilBuild.set("253.*")
        }
    }

    // Optymalizacja pod 8GB RAM: Nie buduj opcji wyszukiwania przy każdym uruchomieniu
    buildSearchableOptions = false
}

// 2. LIMIT RAM DLA TESTOWEJ INSTANCJI IDE
// Bez tego runIde spróbuje zabrać 2-4GB RAM, co przy Twoich 7.9GB zamrozi system
tasks.withType<org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask>().configureEach {
    // Ograniczamy pamięć dla testowego IntelliJ do 1.5GB
    jvmArgs("-Xmx1536m", "-Xms512m")
}