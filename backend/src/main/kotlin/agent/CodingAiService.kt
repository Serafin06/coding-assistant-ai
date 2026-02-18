package agent

import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage

/**
 * Kontrakt AI serwisu — LangChain4j generuje implementację przez proxy.
 * Pamięć jest zarządzana per conversationId przez AiServices.
 */
interface CodingAiService {

    @SystemMessage("""
        You are an expert programming assistant integrated in an IDE.
        Always apply SOLID principles and appropriate design patterns.
        Add brief KDoc/Javadoc comments to every class and method in code you generate.
        Prefer composition over inheritance. Use meaningful names, small functions.
        When given code context, analyze it carefully before responding.
        Be concise and provide working code examples.
    """)
    fun chat(
        @MemoryId conversationId: String,
        @UserMessage message: String
    ): String
}