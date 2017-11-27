package org.neojet.integrate

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.util.TextRange
import org.neojet.NeojetTextFileEditor
import org.neojet.util.getLineEndOffset
import org.neojet.util.getLineStartOffset
import java.util.concurrent.atomic.AtomicBoolean

class DocumentChangeListener(
    private val fileEditor: NeojetTextFileEditor
) : DocumentListener {

    val isAcceptingExternalEdit = AtomicBoolean(false)
    val isTriggeringExternalEdit = AtomicBoolean(false)

    override fun documentChanged(event: DocumentEvent) {
        if (isAcceptingExternalEdit.get()) {
            // ignore document change triggered by external edit
            return
        }

        val start = event.offset
        val end = start + event.oldFragment.length

        val startPos = fileEditor.editor.offsetToLogicalPosition(start)
        val endPos = fileEditor.editor.offsetToLogicalPosition(end)

        val startLine = startPos.line
        val endLine = endPos.line

        val newLines = event.document.getText(TextRange(
            fileEditor.editor.getLineStartOffset(startLine),
            fileEditor.editor.getLineEndOffset(endLine)
        )).split("\n")

        fileEditor.triggerExternalEdit {
            fileEditor.getBuffer()
                .lines(startLine, endLine + 1)
                .replace(newLines)
                .blockingGet()

            fileEditor.nvim.current.window().blockingGet()
                .setCursor(endPos.line + 1, endPos.column + 1)
                .blockingGet()
        }
    }

    override fun beforeDocumentChange(event: DocumentEvent?) {
        // nop
    }

}