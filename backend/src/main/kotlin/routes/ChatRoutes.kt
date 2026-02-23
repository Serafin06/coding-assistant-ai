package routes

import ChatRequest
import agent.AgentOrchestrator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*

/** Definiuje REST endpointy — segregacja interfejsów przez route'y (I z SOLID) */
fun Application.configureChatRoutes(orchestrator: AgentOrchestrator) {
    install(SSE)
    routing {
        route("/api") {

            post("/chat") {
                val request = call.receive<ChatRequest>()
                val response = orchestrator.respond(request)
                call.respond(HttpStatusCode.OK, response)
            }

            post("/chat/stream") {
                val request = call.receive<ChatRequest>()
                call.response.headers.append(HttpHeaders.ContentType, "text/event-stream")
                call.response.headers.append(HttpHeaders.CacheControl, "no-cache")
                call.respondTextWriter {
                    val latch = java.util.concurrent.CountDownLatch(1)
                    orchestrator.streamRespond(
                        request = request,
                        onToken = { token ->
                            write("data: $token\n\n")
                            flush()
                        },
                        onComplete = { conversationId ->
                            write("data: [DONE]:$conversationId\n\n")
                            flush()
                            latch.countDown()
                        },
                        onError = { error ->
                            write("data: [ERROR]:${error.message}\n\n")
                            flush()
                            latch.countDown()
                        }
                    )
                    latch.await()
                }
            }

            delete("/session/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                orchestrator.clearSession(id)
                call.respond(HttpStatusCode.NoContent)
            }

            get("/health") {
                call.respond(mapOf("status" to "ok"))
            }
        }
    }
}