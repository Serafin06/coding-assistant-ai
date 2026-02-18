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
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    intellijPlatform {
        local("C:/Users/RGrabowski/AppData/Local/Programs/IntelliJ IDEA Community Edition 2025.2.3")
        bundledPlugin("com.intellij.java")
        instrumentationTools()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "AI Coding Assistant"
        version = "0.0.1"
        ideaVersion {
            sinceBuild.set("252")
            untilBuild.set("253.*")
        }
    }
    buildSearchableOptions = false
}