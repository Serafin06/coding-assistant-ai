package agent

import ChatRequest
import ChatResponse
import config.OllamaConfig
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore
import java.time.Duration
import java.util.UUID
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import io.ktor.sse.*

class AgentOrchestrator(private val config: OllamaConfig) {

    private val memoryStore = InMemoryChatMemoryStore()

    // Definiujemy providera pamięci jako lambdę
    private val chatMemoryProvider = ChatMemoryProvider { memoryId ->
        MessageWindowChatMemory.builder()
            .id(memoryId)
            .maxMessages(10)
            .chatMemoryStore(memoryStore)
            .build()
    }

    private val aiService: CodingAiService = AiServices.builder(CodingAiService::class.java)
        .chatLanguageModel(
            OllamaChatModel.builder()
                .baseUrl(config.baseUrl)
                .modelName(config.modelName)
                .temperature(config.temperature)
                .timeout(Duration.ofMillis(config.timeout))
                .build()
        )
        .chatMemoryProvider(chatMemoryProvider) // POPRAWKA: nazwa metody
        .build()

    fun respond(request: ChatRequest): ChatResponse {
        val conversationId = request.conversationId ?: UUID.randomUUID().toString()
        val reply = aiService.chat(conversationId, buildMessage(request))
        return ChatResponse(reply = reply, conversationId = conversationId)
    }

    // Dodaj pole obok istniejącego aiService:
    private val streamingAiService: CodingAiService = AiServices.builder(CodingAiService::class.java)
        .streamingChatLanguageModel(
            OllamaStreamingChatModel.builder()
                .baseUrl(config.baseUrl)
                .modelName(config.modelName)
                .temperature(config.temperature)
                .timeout(Duration.ofMillis(config.timeout))
                .build()
        )
        .chatMemoryProvider { memoryId ->
            MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(memoryStore)
                .build()
        }
        .build()

    /** Streamuje odpowiedź token po tokenie przez callback */
    fun streamRespond(
        request: ChatRequest,
        onToken: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        val conversationId = request.conversationId ?: UUID.randomUUID().toString()
        val fullMessage = buildMessage(request)

        streamingAiService.chatStream(conversationId, fullMessage)
            .onPartialResponse { token -> onToken(token) }
            .onCompleteResponse { _ -> onComplete(conversationId) }
            .onError { error -> onError(error) }
            .start()
    }

    // Wydziel budowanie wiadomości (DRY):
    fun buildMessage(request: ChatRequest) = buildString {
        if (!request.codeContext.isNullOrBlank()) {
            append("Code context (${request.language ?: "unknown"}):\n```${request.language ?: ""}\n")
            append(request.codeContext)
            append("\n```\n\n")
        }
        append(request.message)
    }

    fun clearSession(conversationId: String) {
        memoryStore.deleteMessages(conversationId)
    }
}