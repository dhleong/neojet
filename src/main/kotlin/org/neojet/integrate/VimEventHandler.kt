package org.neojet.integrate

import com.intellij.openapi.application.TransactionGuard
import io.neovim.java.Buffer
import io.neovim.java.event.Event
import org.neojet.NeojetTextFileEditor
import org.neojet.events.TextChangedEvent
import org.neojet.gui.UiThreadScheduler
import org.neojet.util.BufferManager
import org.neojet.util.EventDispatcher
import org.neojet.util.HandlesEvent
import org.neojet.util.runWriteActionUndoTransparently
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author dhleong
 */
class VimEventHandler(
    private val buffers: BufferManager
) {
    private val logger = Logger.getLogger("neojet:NJCore")!!

    private val dispatcher = EventDispatcher(this)

    fun dispatch(event: Event<*>) {
        try {
            dispatcher.dispatch(event)
        } catch (e: Throwable) {
            logger.log(Level.WARNING, "Error dispatching $event", e)
        }
    }

    @HandlesEvent fun onTextChanged(event: TextChangedEvent) {
        val buffer = buffers[event.bufferId]
            ?: return logger.log(Level.WARNING, "No buffer for $event")
        val editor = buffers.textFileEditorForBuffer(buffer)
            ?: return logger.log(Level.WARNING, "No editor for $event")

        val change = event.arg
        editor.isModifiedFlag = change.mod

        val isIncremental = change.type == "inc"

        inWriteSafeTxn(editor) {
            if (!isIncremental && change.hasBufWritePostFlag) {
                // the file is persisted to disk; just refresh
                editor.vFile.refresh(true, false)
            } else if (isIncremental) {
                // replace a range
                val doc = editor.editor.document
                val start = doc.getLineStartOffset(change.start)
                val end = doc.getLineEndOffset(change.end)
                doc.replaceString(start, end, change.text)
            } else {
                // load a text range from the buffer
                updateLinesFromBuffer(buffer, change, editor)
            }

            editor.editor.caretModel.primaryCaret
                .moveToOffset(event.value().cursorOffset)

            if (editor.panel.uiModel.currentMode?.isInsert == true) {
                editor.triggerAutoComplete()
            }
        }

    }

    private fun updateLinesFromBuffer(buffer: Buffer, change: TextChangedEvent.Change, editor: NeojetTextFileEditor) {
        buffer.lines(change.start, change.end + 1)
            .get()
            .reduce(StringBuilder()) { builder, line ->
                builder.append(line)
                builder.append('\n')
            }
            .observeOn(UiThreadScheduler.instance)
            .subscribe { lines -> inWriteSafeTxn(editor) {
                lines.setLength(lines.length - 1)
                val doc = editor.editor.document
                val start = doc.getLineStartOffset(change.start)
                val end = doc.getLineEndOffset(change.end)
                doc.replaceString(start, end, lines)
            } }
    }
}

private inline fun inWriteSafeTxn(editor: NeojetTextFileEditor, crossinline block: () -> Unit) {
    TransactionGuard.submitTransaction(editor, Runnable {
        runWriteActionUndoTransparently {
            block()
        }
    })
}
