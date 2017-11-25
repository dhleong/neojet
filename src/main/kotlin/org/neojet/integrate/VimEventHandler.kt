package org.neojet.integrate

import com.intellij.openapi.application.runUndoTransparentWriteAction
import io.neovim.java.event.Event
import org.neojet.events.TextChangedEvent
import org.neojet.util.BufferManager
import org.neojet.util.EventDispatcher
import org.neojet.util.HandlesEvent
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
        System.out.println("onTextChanged @${editor.vFile}: $change")

        editor.isModifiedFlag = change.mod

        runUndoTransparentWriteAction {
            if (!change.mod) {
                // the file is persisted to disk; just refresh
                editor.vFile.refresh(true, false)
            } else if (change.type == "incremental") {
                // replace a range
//                val doc = editor.editor.document
//                val start = doc.getLineStartOffset(change.start)
//                val end = doc.getLineEndOffset(change.end)
//                doc.replaceString(start, end, change.text)
            } else {
                // load a text range from the buffer
                // TODO:
//                buffer.lines(change.start, change.end)
//                    .get()
//                    .reduce(StringBuilder()) { builder, line ->
//                        builder.append(line)
//                    }
//                    .observeOn(UiThreadScheduler.instance)
//                    .subscribe { lines -> runUndoTransparentWriteAction {
//                        val doc = editor.editor.document
//                        val start = doc.getLineStartOffset(change.start)
//                        val end = doc.getLineEndOffset(change.end)
////                        doc.replaceString(start, end, lines)
//                    } }
            }
        }
    }
}