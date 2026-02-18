import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val codeContext: String? = null,
    val language: String? = null,
    val conversationId: String? = null
)

@Serializable
data class ChatResponse(
    val reply: String,
    val conversationId: String
)