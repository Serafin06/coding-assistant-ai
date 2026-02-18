# AI Coding Assistant — IntelliJ & PyCharm Plugin

A fully **free, local, privacy-first** AI coding assistant built with Kotlin, Ktor, and LangChain4j, powered by [Ollama](https://ollama.com). No cloud, no API keys, no costs.

---

## Features

- 💬 Chat panel embedded in IntelliJ IDEA and PyCharm
- 🔍 Sends selected code from the editor as context automatically
- 🧠 Remembers conversation history within a session (up to 10 messages)
- 🛠️ Enforces SOLID principles and design patterns in generated code
- 🌐 Detects language (Kotlin, Python, Java, TypeScript) from open file
- ⌨️ Keyboard shortcut: `Ctrl+Shift+A` to open assistant from editor

---

## Architecture

```
ai-coding-assistant/
├── shared/         # Shared DTOs (ChatRequest, ChatResponse)
├── backend/        # Ktor REST server + LangChain4j + Ollama
└── plugin/         # IntelliJ Platform plugin (UI + HTTP client)
```

**Backend** exposes a local REST API on `localhost:8765`.  
**Plugin** communicates with the backend via OkHttp and renders responses in a Tool Window.

### Key design decisions
- **Composition Root** in `Application.kt` — all dependencies wired in one place
- **Factory pattern** in `AgentOrchestrator` — builds `CodingAiService` with LangChain4j proxy
- **Facade pattern** in `BackendClient` — hides HTTP details from plugin UI
- **SOLID** throughout — each class has a single responsibility, dependencies injected via constructor

---

## Requirements

| Requirement | Details |
|---|---|
| RAM | 8 GB recommended (works on 7.9 GB) |
| GPU | Not required — runs on CPU |
| Java | JDK 17+ |
| Ollama | Installed and running locally |
| IDE | IntelliJ IDEA 2023.3+ or PyCharm 2023.3+ |

---

## Setup

### 1. Install Ollama and pull a model

```bash
# Install Ollama: https://ollama.com/download

# Recommended model for ~8GB RAM:
ollama pull qwen2.5-coder:3b

# Lighter alternative (~800MB):
ollama pull deepseek-coder:1.3b
```

### 2. Clone and configure

```bash
git clone https://github.com/YOUR_USERNAME/ai-coding-assistant.git
cd ai-coding-assistant
```

Edit `backend/src/main/resources/application.conf` if needed:

```hocon
ollama {
    baseUrl     = "http://localhost:11434"
    modelName   = "qwen2.5-coder:3b"   # change to your chosen model
    temperature = 0.2
    timeoutMs   = 120000
}
```

### 3. Run the backend

```bash
cd backend
./gradlew run
```

Verify it works:

```bash
curl -X POST http://localhost:8765/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the Single Responsibility Principle? Give a Kotlin example."}'
```

You should receive a JSON response with `reply` and `conversationId`.

### 4. Run the plugin in development mode

```bash
cd plugin
./gradlew runIde
```

A new IDE instance will open with the plugin installed. Look for the **AI Assistant** panel on the right side.

---

## Usage

1. Open any file in the IDE
2. **Select code** you want to discuss (optional)
3. Open the **AI Assistant** panel on the right, or press `Ctrl+Shift+A`
4. Type your question and press **Send**
5. The assistant receives your selected code as context automatically

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/chat` | Send a message, returns AI reply |
| `DELETE` | `/api/session/{id}` | Clear conversation memory |
| `GET` | `/api/health` | Health check |

---

## Configuration Reference

| Key | Default | Description |
|---|---|---|
| `ollama.baseUrl` | `http://localhost:11434` | Ollama server URL |
| `ollama.modelName` | `qwen2.5-coder:3b` | Model to use |
| `ollama.temperature` | `0.2` | Lower = more deterministic |
| `ollama.timeoutMs` | `120000` | Request timeout (ms) |
| `ktor.deployment.port` | `8765` | Backend port |

---

## Roadmap

- [ ] RAG — index local projects for style-aware suggestions
- [ ] Streaming responses (token by token)
- [ ] Multiple conversation tabs
- [ ] Configurable system prompt from IDE settings UI
- [ ] Support for more languages and frameworks

---

## Tech Stack

- [Kotlin](https://kotlinlang.org/)
- [Ktor](https://ktor.io/) — lightweight async server
- [LangChain4j](https://github.com/langchain4j/langchain4j) — AI service abstraction
- [Ollama](https://ollama.com/) — local LLM runtime
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [OkHttp](https://square.github.io/okhttp/) — HTTP client in plugin

---

## 📞 Contact

- **GitHub**: [@Serafin06](https://github.com/Serafin06)

---

## License

MIT