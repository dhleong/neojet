package org.neojet

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.FileEditorProviderManagerImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import io.neovim.java.Buffer
import org.neojet.gui.NeojetEditorPanel
import java.beans.PropertyChangeListener
import javax.swing.JComponent

val NVIM_BUFFER_KEY = Key<Buffer>("org.neojet.buffer")

/**
 * @author dhleong
 */
class NeojetTextFileEditor(val project: Project, val vFile: VirtualFile)
        : UserDataHolderBase(), FileEditor, TextEditor {

    val editor: TextEditor = createEditor(project, vFile)
    val nvim = NJCore.instance.attach(this)
    val panel = NeojetEditorPanel(getUserData(NVIM_BUFFER_KEY)!!)

    override fun getEditor(): Editor {
        return editor.editor
    }

    override fun canNavigateTo(navigatable: Navigatable): Boolean {
        return false
    }

    override fun navigateTo(navigatable: Navigatable) {

    }

    override fun getComponent(): JComponent {
        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return null
    }

    override fun getName(): String {
        return "neojet"
    }

    override fun setState(state: FileEditorState) {

    }

    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun selectNotify() {

    }

    override fun deselectNotify() {

    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        return null
    }

    override fun dispose() {
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

