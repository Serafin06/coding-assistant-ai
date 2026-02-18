package routes

import ChatRequest
import agent.AgentOrchestrator
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

// Definiuje REST endpointy (I z SOLID — segregacja interfejsów przez route'y)
fun Application.configureChatRoutes(orchestrator: AgentOrchestrator) {
    routing {
        route("/api") {

            post("/chat") {
                val request = call.receive<ChatRequest>()
                val response = orchestrator.respond(request)
                call.respond(HttpStatusCode.OK, response)
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