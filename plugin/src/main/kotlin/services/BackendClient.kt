package services

import ChatRequest
import ChatResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.time.Duration

/** Fasada HTTP — ukrywa szczegóły komunikacji z backendem */
class BackendClient(private val baseUrl: String = "http://localhost:8765") {

    private val client = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(300))
        .readTimeout(Duration.ofSeconds(300))
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun chat(request: ChatRequest): ChatResponse {
        val body = json.encodeToString(ChatRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())
        val httpRequest = Request.Builder().url("$baseUrl/api/chat").post(body).build()
        client.newCall(httpRequest).execute().use { response ->
            val responseBody = response.body?.string() ?: throw RuntimeException("Empty response")
            return json.decodeFromString(ChatResponse.serializer(), responseBody)
        }
    }

    /** Streaming — wywołuje onToken dla każdego tokenu, onComplete gdy skończone */
    fun chatStream(
        request: ChatRequest,
        onToken: (String) -> Unit,
        onComplete: (conversationId: String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val body = json.encodeToString(ChatRequest.serializer(), request)
            .toRequestBody("application/json".toMediaType())
        val httpRequest = Request.Builder()
            .url("$baseUrl/api/chat/stream")
            .post(body)
            .header("Accept", "text/event-stream")
            .build()

        EventSources.createFactory(client).newEventSource(httpRequest, object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                if (data.startsWith("[DONE]:")) {
                    onComplete(data.removePrefix("[DONE]:"))
                } else {
                    onToken(data)
                }
            }
            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                t?.let { onError(it) }
            }
        })
    }

    fun health() {
        val request = Request.Builder().url("$baseUrl/api/health").get().build()
        client.newCall(request).execute().use { if (!it.isSuccessful) throw RuntimeException("Backend unhealthy") }
    }

    fun clearSession(conversationId: String) {
        val request = Request.Builder().url("$baseUrl/api/session/$conversationId").delete().build()
        client.newCall(request).execute().close()
    }
}