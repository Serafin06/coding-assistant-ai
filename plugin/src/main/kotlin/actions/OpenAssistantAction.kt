package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager


// Akcja wywoływana z menu kontekstowego edytora
class OpenAssistantAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("AI Assistant") ?: return
        toolWindow.show()
    }

    override fun update(e: AnActionEvent) {
        // Pokazuj akcję tylko gdy jest otwarty edytor
        e.presentation.isEnabled = e.project != null &&
                FileEditorManager.getInstance(e.project!!).selectedTextEditor != null
    }
}