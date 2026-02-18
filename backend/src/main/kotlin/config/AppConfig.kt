package config

import com.typesafe.config.ConfigFactory

// Odpowiada za wczytanie konfiguracji z application.conf
data class OllamaConfig(
    val baseUrl: String,   // np. "http://localhost:11434"
    val modelName: String, // np. "qwen2.5-coder:3b"
    val temperature: Double,
    val timeout: Long      // ms
)

object AppConfig {
    private val config = ConfigFactory.load()

    val ollama = OllamaConfig(
        baseUrl    = config.getString("ollama.baseUrl"),
        modelName  = config.getString("ollama.modelName"),
        temperature = config.getDouble("ollama.temperature"),
        timeout    = config.getLong("ollama.timeoutMs")
    )

    val serverPort: Int = config.getInt("ktor.deployment.port")
}