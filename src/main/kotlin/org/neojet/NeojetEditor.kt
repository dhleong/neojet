package org.neojet

import com.intellij.openapi.Disposable
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

    override fun dispose() {
        (delegate as EditorImpl).disposable.dispose()
    }

    override fun getComponent(): JComponent = textFileEditor.component

    override fun getContentComponent(): JComponent = textFileEditor.panel

    // TODO ?

}