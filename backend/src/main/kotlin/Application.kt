import agent.AgentOrchestrator
import config.AppConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import routes.configureChatRoutes


// Entry point — składa wszystko razem (wzorzec Composition Root)
fun main() {
    embeddedServer(Netty, port = AppConfig.serverPort) {
        install(ContentNegotiation) { json() }
        install(CORS) { anyHost() } // plugin łączy się lokalnie

        val orchestrator = AgentOrchestrator(AppConfig.ollama)
        configureChatRoutes(orchestrator)

    }.start(wait = true)
}