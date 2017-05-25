package org.neojet

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import io.neovim.java.Neovim
import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.reactivex.disposables.CompositeDisposable
import org.neojet.util.buffer
import org.neojet.util.bufferedRedrawEvents
import org.neojet.util.input
import java.awt.Component
import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent
import javax.swing.JComponent

/**
 * @author dhleong
 */

val NEOJET_ENHANCED_EDITOR = Key<NeojetEnhancedEditorFacade>("org.neojet.enhancedEditor")

class NeojetEnhancedEditorFacade private constructor(val editor: EditorEx) : Disposable {
    companion object {
        fun install(editor: Editor): NeojetEnhancedEditorFacade {
            if (!(editor is EditorImpl) || (editor is TextEditor)) {
                throw IllegalArgumentException("$editor is not an EditorEx or TextEditor")
            }

            val facade = NeojetEnhancedEditorFacade(editor)
            editor.putUserData(NEOJET_ENHANCED_EDITOR, facade)

            if (editor is EditorImpl) {
                Disposer.register(editor.disposable, facade)
            } else if (editor is TextEditor) {
                Disposer.register(editor, facade)
            }

            return facade
        }
    }

    val keyEventDispatcher: KeyEventDispatcher = KeyEventDispatcher {
        val isForOurComponent = it?.component?.belongsTo(editor.component) ?: false
        if (isForOurComponent && it.id == KeyEvent.KEY_TYPED) {
            dispatchTypedKey(it)
            true // consume
        } else if (isForOurComponent) {
            // TODO handle held keys, for example
            false
        } else {
            // not for our editor; ignore
            false
        }
    }

    val nvim: Neovim = NJCore.instance.attach(editor)
    val subs = CompositeDisposable()
    val dispatcher = EventDispatcher(this)

    init {
        subs.add(
            nvim.bufferedRedrawEvents()
                .subscribe(this::dispatchRedrawEvents)
        )
    }

    override fun dispose() {
        subs.dispose()
    }

    fun dispatchTypedKey(e: KeyEvent) {
        val buffer = editor.buffer
        System.out.println("Dispatch typed: $e on $buffer")
        nvim.input(e).subscribe()
    }

    /*
     * Notifications from nvim
     */

    @HandlesEvent fun cursorMoved(event: CursorGotoEvent) {
        System.out.println("cursorMoved($event)")
        event.value[0].let {
            editor.caretModel.primaryCaret.moveToLogicalPosition(
                LogicalPosition(it.row(), it.col())
            )
        }
    }

    internal fun dispatchRedrawEvents(events: List<RedrawSubEvent<*>>) {
        events.forEach(dispatcher::dispatch)
    }
}


private fun Component.belongsTo(parentMaybe: JComponent): Boolean {

    var self: Component? = this
    do {
        if (self?.parent == parentMaybe) return true

        self = self?.parent
    } while (self != null)

    return false
}
