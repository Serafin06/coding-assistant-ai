package services

import ChatRequest
import ChatResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.json.Json
import java.time.Duration


// Odpowiada wyłącznie za komunikację HTTP z Ktor backendem
// Wzorzec Facade — ukrywa szczegóły HTTP przed resztą pluginu
class BackendClient(
    private val baseUrl: String = "http://localhost:8765" // <-- port z application.conf
) {
    private val client = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(120))
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun chat(request: ChatRequest): ChatResponse {
        val body = json.encodeToString(ChatRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url("$baseUrl/api/chat")
            .post(body)
            .build()

        client.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string() ?: throw RuntimeException("Empty response")
            return json.decodeFromString(ChatResponse.serializer(), responseBody)
        }
    }
}