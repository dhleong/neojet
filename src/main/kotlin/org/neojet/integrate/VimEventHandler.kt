package org.neojet.integrate

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

        System.out.println("onTextChanged @${editor.vFile}")

        // TODO update the file; changes could be provided with the event
//        runUndoTransparentWriteAction {
//            PsiManager.getInstance(editor.project)
//                .findFile(editor.vFile)!!.subtreeChanged()
//        }
    }
}