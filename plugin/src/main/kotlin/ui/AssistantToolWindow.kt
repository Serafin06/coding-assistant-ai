package ui

import ChatRequest
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import services.BackendClient
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.text.*

/** Fabryka panelu — rejestrowana w plugin.xml jako toolWindow */
class AssistantToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = AssistantPanel(project)
        val content = toolWindow.contentManager.factory.createContent(panel.component, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

/** Główny panel UI — obsługuje streaming, formatowanie kodu i kopiowanie */
class AssistantPanel(private val project: Project) {
    private val client = BackendClient()
    private var conversationId: String? = null
    private var lastAiResponse = StringBuilder()

    // JTextPane zamiast JTextArea — obsługuje style (bold, monospace)
    private val outputPane = JTextPane().apply {
        isEditable = false
        background = Color(43, 43, 43)
        foreground = Color(220, 220, 220)
    }

    private val normalStyle: Style = outputPane.addStyle("normal", null).apply {
        StyleConstants.setFontFamily(this, "SansSerif")
        StyleConstants.setFontSize(this, 12)
        StyleConstants.setForeground(this, Color(220, 220, 220))
    }

    private val codeStyle: Style = outputPane.addStyle("code", null).apply {
        StyleConstants.setFontFamily(this, "JetBrains Mono, Monospaced")
        StyleConstants.setFontSize(this, 12)
        StyleConstants.setForeground(this, Color(169, 220, 118))
        StyleConstants.setBackground(this, Color(30, 30, 30))
    }

    private val userStyle: Style = outputPane.addStyle("user", null).apply {
        StyleConstants.setFontFamily(this, "SansSerif")
        StyleConstants.setFontSize(this, 12)
        StyleConstants.setBold(this, true)
        StyleConstants.setForeground(this, Color(100, 180, 255))
    }

    val component: JPanel = JPanel(BorderLayout()).apply {
        val inputField = JTextField()
        val sendButton = JButton("Send")
        val clearButton = JButton("Clear")
        val copyButton = JButton("Copy last")

        checkBackendConnection()

        sendButton.addActionListener {
            val message = inputField.text.takeIf { it.isNotBlank() } ?: return@addActionListener
            inputField.text = ""
            sendButton.isEnabled = false
            lastAiResponse.clear()

            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val hasSelection = editor?.selectionModel?.hasSelection() == true
            val codeContext = editor?.selectionModel?.selectedText ?: editor?.document?.text
            val contextInfo = when {
                hasSelection -> "(selected)"
                editor != null -> "(full file)"
                else -> "(no context)"
            }

            appendStyledText("\n\nYou $contextInfo: $message\n", userStyle)
            appendStyledText("\nAI: ", normalStyle)

            client.chatStream(
                request = ChatRequest(
                    message = message,
                    codeContext = codeContext,
                    language = detectLanguage(project),
                    conversationId = conversationId
                ),
                onToken = { token ->
                    lastAiResponse.append(token)
                    SwingUtilities.invokeLater { appendToken(token) }
                },
                onComplete = { id ->
                    conversationId = id
                    SwingUtilities.invokeLater { sendButton.isEnabled = true }
                },
                onError = { e ->
                    SwingUtilities.invokeLater {
                        appendStyledText("\n[Error: ${e.message}]", normalStyle)
                        sendButton.isEnabled = true
                    }
                }
            )
        }

        clearButton.addActionListener {
            outputPane.document.remove(0, outputPane.document.length)
            conversationId?.let { id ->
                ApplicationManager.getApplication().executeOnPooledThread {
                    runCatching { client.clearSession(id) }
                }
            }
            conversationId = null
            lastAiResponse.clear()
        }

        copyButton.addActionListener {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(lastAiResponse.toString()), null)
        }

        inputField.addActionListener { sendButton.doClick() }

        add(JScrollPane(outputPane), BorderLayout.CENTER)
        add(JPanel(BorderLayout()).apply {
            add(inputField, BorderLayout.CENTER)
            add(JPanel(FlowLayout(FlowLayout.RIGHT, 2, 0)).apply {
                add(copyButton)
                add(clearButton)
                add(sendButton)
            }, BorderLayout.EAST)
        }, BorderLayout.SOUTH)
    }

    /** Dodaje token — rozpoznaje bloki kodu i stosuje odpowiedni styl */
    private val codeBlockBuffer = StringBuilder()
    private var insideCodeBlock = false

    private fun appendToken(token: String) {
        codeBlockBuffer.append(token)
        val text = codeBlockBuffer.toString()

        if (!insideCodeBlock && text.contains("```")) {
            val parts = text.split("```", limit = 2)
            appendStyledText(parts[0], normalStyle)
            codeBlockBuffer.clear()
            if (parts.size > 1) {
                insideCodeBlock = true
                codeBlockBuffer.append(parts[1])
                appendToken("") // re-process remainder
            }
        } else if (insideCodeBlock && text.contains("```")) {
            val parts = text.split("```", limit = 2)
            appendStyledText(parts[0], codeStyle)
            codeBlockBuffer.clear()
            insideCodeBlock = false
            if (parts.size > 1) {
                codeBlockBuffer.append(parts[1])
                appendToken("")
            }
        } else if (token.isNotEmpty() && !text.contains("`")) {
            appendStyledText(text, if (insideCodeBlock) codeStyle else normalStyle)
            codeBlockBuffer.clear()
        }
    }

    private fun appendStyledText(text: String, style: Style) {
        val doc = outputPane.styledDocument
        doc.insertString(doc.length, text, style)
        outputPane.caretPosition = doc.length
    }

    private fun checkBackendConnection() {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                client.health()
                SwingUtilities.invokeLater {
                    appendStyledText("✓ Backend connected. Ask me anything!\n", normalStyle)
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    appendStyledText("✗ Backend not running!\njava -jar backend/build/libs/backend-all.jar\n", normalStyle)
                }
            }
        }
    }

    private fun detectLanguage(project: Project): String {
        val file = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
        return when (file?.extension?.lowercase()) {
            "kt", "kts" -> "kotlin"
            "py" -> "python"
            "java" -> "java"
            "js", "ts" -> "typescript"
            else -> "unknown"
        }
    }
}