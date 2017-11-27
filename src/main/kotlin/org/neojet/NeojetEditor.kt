package org.neojet

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import javax.swing.JComponent

/**
 * @author dhleong
 */
class NeojetEditor(
    private val textFileEditor: NeojetTextFileEditor,
    val delegate: EditorEx
) : EditorEx by delegate, Disposable {

    /** see [CaretModelDelegate] */
    private val caretModelDelegate = CaretModelDelegate(
        delegate.caretModel,
        this
    )

    override fun dispose() {
        (delegate as EditorImpl).disposable.dispose()
    }

    override fun getCaretModel(): CaretModel = caretModelDelegate

    override fun getComponent(): JComponent = textFileEditor.component

    override fun getContentComponent(): JComponent = textFileEditor.panel

    override fun getDataContext(): DataContext = DataContext { key ->
        if (key == CommonDataKeys.EDITOR.name) {
            NeojetEditor@this
        } else {
            delegate.dataContext.getData(key)
        }
    }

    fun performActionById(actionId: String) {
        val mgr = ActionManager.getInstance()
        val action = mgr.getAction(actionId)

        val event = AnActionEvent.createFromAnAction(action, null, "", dataContext)
        action.actionPerformed(event)
    }

}

/**
 * Due to the way some Lookup/Completion APIs retrieve
 *  the Editor from the Cursor to check the current
 *  Completion, we need to wrap the actual CaretModel
 *  and its Caret instances so they provide our
 *  [NeojetEditor], since it provides the correct
 *  Component instance.
 */
private class CaretModelDelegate(
    private val delegate: CaretModel,
    private val neojetEditor: NeojetEditor
): CaretModel by delegate {

    override fun getCaretAt(pos: VisualPosition): Caret? =
        delegate.getCaretAt(pos)?.delegated()

    override fun getAllCarets(): MutableList<Caret> =
        delegate.allCarets.map { it.delegated() }.toMutableList()

    override fun getCurrentCaret(): Caret =
        delegate.currentCaret.delegated()

    override fun getPrimaryCaret(): Caret =
        delegate.primaryCaret.delegated()

    private fun Caret.delegated() =
        this as? CaretDelegate
            ?: CaretDelegate(this, neojetEditor)
}

private class CaretDelegate(delegate: Caret, private val editor: NeojetEditor) : Caret by delegate {
    override fun getEditor(): Editor = editor
}