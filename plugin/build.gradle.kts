plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij.platform")
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":shared"))

    intellijPlatform {
        // POPRAWKA: Używamy poprawnej nazwy parametru 'localPath' lub podajemy sam argument.
        // Usuwamy też 'type.set', bo wersja lokalna jest wykrywana automatycznie.
        local(localPath = "C:/Users/RGrabowski/AppData/Local/Programs/IntelliJ IDEA Community Edition 2025.2.3")
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "AI Coding Assistant"
        // Dostosowujemy zakres wersji do Twojej lokalnej instalacji (2025.2.3 -> build 252.*)
        ideaVersion {
            sinceBuild.set("252")
            untilBuild.set("253.*")
        }
    }
}