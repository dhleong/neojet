package org.neojet

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.event.CaretAdapter
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.DocumentAdapter
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import io.neovim.java.Neovim
import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.EolClearEvent
import io.neovim.java.event.redraw.ModeChangeEvent
import io.neovim.java.event.redraw.ModeInfoSetEvent
import io.neovim.java.event.redraw.PutEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.neovim.java.util.ModeInfo
import io.reactivex.disposables.CompositeDisposable
import org.neojet.util.buffer
import org.neojet.util.bufferedRedrawEvents
import org.neojet.util.getLineEndOffset
import org.neojet.util.getTextCells
import org.neojet.util.inWriteAction
import org.neojet.util.input
import org.neojet.util.runUndoTransparently
import java.awt.Component
import java.awt.KeyEventDispatcher
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
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
            // FIXME should we just let it go through if in insert mode?
            // FIXME That might be the more sane way to handle it, though
            //  we'd lose vim iabbrevs....
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

    val caretMovedListener = object : CaretAdapter() {
        override fun caretPositionChanged(e: CaretEvent?) {
            if (e != null && !movingCursor) {
                val line = e.newPosition.line + 1 // 0-indexed to 1-indexed
                val column = e.newPosition.column

                nvim.current.window()
                    .flatMap { window ->
                        window.setCursor(line, column)
                    }
                    .subscribe()
            }
        }
    }

    var cells = editor.getTextCells()

    val nvim: Neovim = NJCore.instance.attach(editor, this)
    val subs = CompositeDisposable()
    val dispatcher = EventDispatcher(this)

    internal lateinit var modes: List<ModeInfo>
    internal var mode: ModeInfo? = null

    var editingDocumentFromVim = false
    var movingCursor = false
    var cursorRow: Int = 0
    var cursorCol: Int = 0

    init {
        editor.caretModel.addCaretListener(caretMovedListener)
        editor.contentComponent.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                nvim.current.bufferSet(editor.buffer).subscribe()
            }
        })

        editor.document.addDocumentListener(object : DocumentAdapter() {
            override fun documentChanged(e: DocumentEvent?) {
                if (editingDocumentFromVim) return

                e?.let {
                    // TODO: IntelliJ edited the document unexpectedly
                    System.out.println(
                        "Document changed @${it.offset}: `${it.oldFragment}` -> `${it.newFragment}`")
                    System.out.println("${it.oldLength} -> ${it.newLength}")
                }
            }
        }, this)

        subs.add(
            nvim.bufferedRedrawEvents()
                .subscribe(this::dispatchRedrawEvents)
        )
    }

    override fun dispose() {
        subs.dispose()

        editor.caretModel.removeCaretListener(caretMovedListener)
    }

    fun dispatchTypedKey(e: KeyEvent) {
//        val buffer = editor.buffer
//        nvim.current.bufferSet(buffer)
//            .flatMap { nvim.input(e) }
//            .subscribe()

        nvim.input(e).subscribe()
    }

    /*
     * Notifications from nvim
     */

    @HandlesEvent fun clearToEol(event: EolClearEvent) {
        if (DumbService.getInstance(editor.project!!).isDumb) return

        val line = cursorCol
        val start = editor.logicalPositionToOffset(LogicalPosition(cursorRow, cursorCol))
        val lineEndOffset = editor.getLineEndOffset(line)
        val end = minOf(
            editor.document.textLength - 1,
            lineEndOffset
        )

        if (end < start) {
            // usually for drawing status line, etc.
            return
        }

        System.out.println("clearEol($start, $end)")
        editor.document.deleteString(start, end)
    }

    @HandlesEvent fun cursorMoved(event: CursorGotoEvent) {
        movingCursor = true

        event.value[0].let {
            cursorRow = it.row()
            cursorCol = it.col()
            editor.caretModel.primaryCaret.moveToLogicalPosition(
                LogicalPosition(cursorRow, cursorCol)
            )
        }

        movingCursor = false
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

        val line = cursorRow
        if (line >= cells.height() - 1) {
            // status line or something
            return
        }

        val lineText = event.bytesToCharSequence()
        val lineEndOffset = editor.getLineEndOffset(line)
        val start = editor.logicalPositionToOffset(LogicalPosition(line, cursorCol))
        val delta = lineEndOffset - start
        val end = minOf(
            editor.document.textLength - 1,
            start + minOf(event.value.size, delta)
        )

        if (lineEndOffset < start) {
            // usually for drawing status line, etc.
            System.out.println("Ignore put @$line,$cursorCol")
            return
        }

        if (start == end) {
            System.out.println("INSERT($start) <- $lineText")
            editor.document.insertString(start, lineText)
        } else {
            System.out.println("REPLACE($start, $end) <- $lineText")
            editor.document.replaceString(start, end, lineText)
        }
        cursorCol += event.value.size
    }

    private fun updateCursor(mode: ModeInfo) {
        val useBlock = (mode.cursorShape == ModeInfo.CursorShape.BLOCK)
        editor.settings.isBlockCursor = useBlock
    }

    internal fun dispatchRedrawEvents(events: List<RedrawSubEvent<*>>) {
        editDocumentFromVim {
            events.forEach(dispatcher::dispatch)
        }
    }

    private inline fun editDocumentFromVim(crossinline edits: () -> Unit) {
        editingDocumentFromVim = true
        inWriteAction {
            runUndoTransparently {
                edits()
            }
        }
        editingDocumentFromVim = false
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

