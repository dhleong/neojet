package org.neojet

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.codeInsight.daemon.impl.TextEditorBackgroundHighlighter
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.MarkupModelEx
import com.intellij.openapi.editor.ex.RangeHighlighterEx
import com.intellij.openapi.editor.impl.event.MarkupModelListener
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.FileEditorProviderManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import io.neovim.java.Buffer
import org.neojet.gui.NeojetEditorPanel
import org.neojet.gui.NeojetShortcutKeyAction
import java.beans.PropertyChangeListener
import javax.swing.JComponent

val NVIM_BUFFER_KEY = Key<Buffer>("org.neojet.buffer")

/**
 * @author dhleong
 */
class NeojetTextFileEditor(
    private val project: Project,
    val vFile: VirtualFile
)
        : UserDataHolderBase(), FileEditor, TextEditor {

    private val editor: TextEditor = createEditor(project, vFile)

    val panel = NeojetEditorPanel()

    val nvim = NJCore.instance.attach(this)

    private val myBackgroundHighlighter by lazy(LazyThreadSafetyMode.NONE) {
        TextEditorBackgroundHighlighter(project, getEditor())
    }

    init {
        NeojetShortcutKeyAction.install(this)

        (getEditor().markupModel as MarkupModelEx)
            .addMarkupModelListener(this, object : MarkupModelListener {
                override fun attributesChanged(highlighter: RangeHighlighterEx, renderersChanged: Boolean, fontStyleOrColorChanged: Boolean) {
                    System.out.println("Attrs changed: $highlighter")
                }

                override fun beforeRemoved(highlighter: RangeHighlighterEx) {
                    System.out.println("Remove $highlighter")
                }

                override fun afterAdded(highlighter: RangeHighlighterEx) {
                    System.out.println("Add $highlighter")
                }

            })
    }

    override fun getEditor(): Editor {
        return editor.editor
    }

    override fun canNavigateTo(navigatable: Navigatable): Boolean {
        return false
    }

    override fun navigateTo(navigatable: Navigatable) {
        System.out.println("TODO navigate to $navigatable")
    }

    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent? = panel

    override fun getName(): String = "neojet"

    override fun setState(state: FileEditorState) {

    }

    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun selectNotify() {
        // TODO possibly, each Editor should have its own tabpage?
        //  that would automatically keep the right cursor position
        //  (swapping the buffer in the active window does not do this!)
        val buffer = getEditor().getUserData(NVIM_BUFFER_KEY)
        buffer?.let {
            nvim.current.bufferSet(it)
                .blockingGet()
        }
    }

    override fun deselectNotify() {

    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        System.out.println("TODO getBackgroundHighlighter")
        return myBackgroundHighlighter
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        System.out.println("TODO getCurrentLocation")
        return null
    }

    override fun dispose() {
        NeojetShortcutKeyAction.uninstall(this)
        panel.dispose()
    }

}

private fun createEditor(project: Project, vFile: VirtualFile): TextEditor {
    val provider = getProvider(project, vFile)

    if (provider != null) {
        val editor = provider.createEditor(project, vFile)
        if (editor is TextEditor) {
            return editor
        }
    }

    throw IllegalStateException()
}

private fun getProvider(project: Project, vFile: VirtualFile): FileEditorProvider? {
    val providers = FileEditorProviderManagerImpl.getInstance().getProviders(project, vFile)
    return providers.firstOrNull { it !is NeojetEditorProvider }
}

