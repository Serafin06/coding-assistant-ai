package agent

import com.aiassistant.config.OllamaConfig
import com.aiassistant.shared.ChatRequest
import com.aiassistant.shared.ChatResponse
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore
import java.time.Duration
import java.util.UUID

/**
 * Orkiestruje wywołania do modelu AI.
 * Wzorzec Factory — buduje CodingAiService z konfiguracją.
 * Pamięć rozmów przechowywana in-memory per sesja.
 */
class AgentOrchestrator(private val config: OllamaConfig) {

    // Współdzielony store pamięci — jeden na wszystkie sesje
    private val memoryStore = InMemoryChatMemoryStore()

    private val aiService: CodingAiService = AiServices.builder(CodingAiService::class.java)
        .chatLanguageModel(
            OllamaChatModel.builder()
                .baseUrl(config.baseUrl)
                .modelName(config.modelName)
                .temperature(config.temperature)
                .timeout(Duration.ofMillis(config.timeout))
                .build()
        )
        .chatMemoryProviderFactory { memoryId ->
            MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(memoryStore)
                .build()
        }
        .build()

    /** Buduje wiadomość z opcjonalnym kontekstem kodu i deleguje do AI serwisu. */
    fun respond(request: ChatRequest): ChatResponse {
        val conversationId = request.conversationId ?: UUID.randomUUID().toString()

        val fullMessage = buildString {
            if (!request.codeContext.isNullOrBlank()) {
                append("Code context (${request.language ?: "unknown"}):\n```${request.language ?: ""}\n")
                append(request.codeContext)
                append("\n```\n\n")
            }
            append(request.message)
        }

        val reply = aiService.chat(conversationId, fullMessage)
        return ChatResponse(reply = reply, conversationId = conversationId)
    }

    /** Czyści pamięć podanej sesji. */
    fun clearSession(conversationId: String) {
        memoryStore.deleteMessages(conversationId)
    }
}