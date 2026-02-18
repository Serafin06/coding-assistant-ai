package ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import services.BackendClient
import java.awt.BorderLayout
import javax.swing.*


// Panel boczny w IDE — odpowiada wyłącznie za UI
class AssistantToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = AssistantPanel(project)
        val content = toolWindow.contentManager.factory
            .createContent(panel.component, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class AssistantPanel(private val project: Project) {
    private val client = BackendClient()
    private var conversationId: String? = null

    val component: JPanel = JPanel(BorderLayout()).apply {
        val outputArea = JTextArea().apply {
            isEditable = false
            lineWrap = true
        }
        val inputField = JTextField()
        val sendButton = JButton("Send")

        sendButton.addActionListener {
            val message = inputField.text.takeIf { it.isNotBlank() } ?: return@addActionListener
            inputField.text = ""

            // Pobierz zaznaczony kod z edytora
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val selectedCode = editor?.selectionModel?.selectedText
            val language = detectLanguage(project)

            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val response = client.chat(
                        ChatRequest(
                            message = message,
                            codeContext = selectedCode,
                            language = language,
                            conversationId = conversationId
                        )
                    )
                    conversationId = response.conversationId
                    SwingUtilities.invokeLater {
                        outputArea.append("\n\nYou: $message\n\nAI: ${response.reply}")
                    }
                } catch (e: Exception) {
                    SwingUtilities.invokeLater {
                        outputArea.append("\n[Error: ${e.message}]")
                    }
                }
            }
        }

        add(JScrollPane(outputArea), BorderLayout.CENTER)
        add(JPanel(BorderLayout()).apply {
            add(inputField, BorderLayout.CENTER)
            add(sendButton, BorderLayout.EAST)
        }, BorderLayout.SOUTH)
    }

    private fun detectLanguage(project: Project): String {
        val file = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
        return when (file?.extension?.lowercase()) {
            "kt", "kts" -> "kotlin"
            "py"        -> "python"
            "java"      -> "java"
            "js", "ts"  -> "typescript"
            else        -> "unknown"
        }
    }
}