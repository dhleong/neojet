package org.neojet

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import io.neovim.java.Neovim
import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.ModeChangeEvent
import io.neovim.java.event.redraw.ModeInfoSetEvent
import io.neovim.java.event.redraw.PutEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.neovim.java.util.ModeInfo
import io.reactivex.disposables.CompositeDisposable
import org.neojet.util.buffer
import org.neojet.util.bufferedRedrawEvents
import org.neojet.util.inWriteAction
import org.neojet.util.input
import org.neojet.util.runUndoTransparently
import java.awt.Component
import java.awt.KeyEventDispatcher
import java.awt.event.KeyEvent
import javax.swing.JComponent

/**
 * @author dhleong
 */

val NEOJET_ENHANCED_EDITOR = Key<NeojetEnhancedEditorFacade>("org.neojet.enhancedEditor")

class NeojetEnhancedEditorFacade private constructor(val editor: Editor) : Disposable {
    companion object {
        fun install(editor: Editor): NeojetEnhancedEditorFacade {
            if (!(editor is EditorImpl || editor is TextEditor)) {
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

    internal lateinit var modes: List<ModeInfo>
    internal var mode: ModeInfo? = null

    var cursorRow: Int = 0
    var cursorCol: Int = 0

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

        nvim.current.bufferSet(buffer)
            .flatMap { nvim.input(e) }
            .subscribe()
    }

    /*
     * Notifications from nvim
     */

    @HandlesEvent fun cursorMoved(event: CursorGotoEvent) {
        event.value[0].let {
            cursorRow = it.row()
            cursorCol = it.col()
            editor.caretModel.primaryCaret.moveToLogicalPosition(
                LogicalPosition(cursorRow, cursorCol)
            )
        }
    }

    @HandlesEvent fun modeInfoSet(event: ModeInfoSetEvent) {
        modes = event.value[0].modes
    }

    @HandlesEvent fun modeChange(event: ModeChangeEvent) {
        modes[event.value[0].modeIndex].let {
            mode = it
            updateCursor(it)
        }
    }

    @HandlesEvent fun put(event: PutEvent) {
        if (DumbService.getInstance(editor.project!!).isDumb) return

        val start = editor.caretModel.primaryCaret.offset
        val line = editor.offsetToLogicalPosition(start).line
        val nextLineStartOffset = editor.logicalPositionToOffset(LogicalPosition(line + 1, 0))
        val delta = nextLineStartOffset - 1 - start // -1 to get end of current line
        val end = minOf(
            editor.document.textLength - 1,
            start + minOf(event.value.size, delta)
        )

        if (end < start) {
            // usually for drawing status line, etc.
//            System.out.println("Ignore $event at $start (> $end)")
            return
        }

        inWriteAction {
            runUndoTransparently {
                editor.document.replaceString(start, end, event.bytesToCharSequence())
            }
        }
    }

    private fun updateCursor(mode: ModeInfo) {
        val useBlock = (mode.cursorShape == ModeInfo.CursorShape.BLOCK)
        editor.settings.isBlockCursor = useBlock
    }

    internal fun dispatchRedrawEvents(events: List<RedrawSubEvent<*>>) {
        events.forEach(dispatcher::dispatch)
    }
}


private fun PutEvent.bytesToCharSequence(): CharSequence {
    return value.fold(StringBuilder(value.size), { buffer, value ->
        buffer.append(value.value)
    })
}

private fun Component.belongsTo(parentMaybe: JComponent): Boolean {

    var self: Component? = this
    do {
        if (self?.parent == parentMaybe) return true

        self = self?.parent
    } while (self != null)

    return false
}
