package org.neojet

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl
import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInsight.daemon.impl.TextEditorBackgroundHighlighter
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
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
import io.neovim.java.Neovim
import org.neojet.gui.NeojetEditorPanel
import org.neojet.gui.NeojetShortcutKeyAction
import org.neojet.gui.demandFocus
import org.neojet.integrate.CompletionStateWatcher
import org.neojet.integrate.DocumentChangeListener
import java.beans.PropertyChangeListener
import javax.swing.JComponent

val NVIM_BUFFER_KEY = Key<Buffer>("org.neojet.buffer")

/**
 * @author dhleong
 */
class NeojetTextFileEditor(
    val project: Project,
    val vFile: VirtualFile
) : UserDataHolderBase(), FileEditor, TextEditor {

    private val editor = NeojetEditor(
        this,
        createEditor(project, vFile).editor as EditorEx
    )

    val panel = NeojetEditorPanel(project, editor).also {

        // hide completion lookup when exiting insert mode
        it.uiModel.addModeChangeListener { mode ->
            if (!mode.isInsert) {
                cancelAutoComplete()
            }
        }

    }

    val nvim = NJCore.instance.attach(this)

    var isModifiedFlag = false

    private val myBackgroundHighlighter by lazy(LazyThreadSafetyMode.NONE) {
        TextEditorBackgroundHighlighter(project, getEditor())
    }

    private val documentChangeListener = DocumentChangeListener(this)

    init {
        NeojetShortcutKeyAction.install(this)

        val buffer = getBuffer()

        val markupListener = object : MarkupModelListener {
            override fun attributesChanged(highlighter: RangeHighlighterEx, renderersChanged: Boolean, fontStyleOrColorChanged: Boolean) {
                System.out.println("Attrs changed: #${highlighter.id} $highlighter / ${highlighter.errorStripeTooltip}")
                // TODO update, I guess?
            }

            override fun beforeRemoved(highlighter: RangeHighlighterEx) {
                nvim.highlightCmd(buffer, "delete", highlighter)
            }

            override fun afterAdded(highlighter: RangeHighlighterEx) {
                nvim.highlightCmd(buffer, "create", highlighter)
            }

        }

        getEditor().document.addDocumentListener(documentChangeListener)

        val parentDisposable = this
        (getEditor() as EditorEx).apply {
            markupModel.addMarkupModelListener(parentDisposable, markupListener)
            filteredDocumentMarkupModel.addMarkupModelListener(parentDisposable, markupListener)
        }
    }

    fun getBuffer(): Buffer =
        getEditor().getUserData(NVIM_BUFFER_KEY)!!

    // NOTE: various IntelliJ APIs implicitly require an EditorImpl
    //  instance, so we can't return our "real" Editor here (but we
    //  *do* need the "real" one for other APIs, like when we
    //  trigger auto completion)
    override fun getEditor(): Editor = editor.delegate

    override fun canNavigateTo(navigatable: Navigatable): Boolean {
        return false
    }

    override fun navigateTo(navigatable: Navigatable) {
        System.out.println("TODO navigate to $navigatable")
    }

    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent = panel

    override fun getName(): String = "neojet"

    override fun setState(state: FileEditorState) {

    }

    override fun isModified(): Boolean = isModifiedFlag

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

        panel.demandFocus()
    }

    override fun deselectNotify() {

    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {

    }

    override fun getBackgroundHighlighter() = myBackgroundHighlighter

    override fun getCurrentLocation(): FileEditorLocation? {
        System.out.println("TODO getCurrentLocation")
        return null
    }

    override fun dispose() {
        NeojetShortcutKeyAction.uninstall(this)
        panel.dispose()
    }

    private fun cancelAutoComplete() {
        CompletionServiceImpl.getCompletionService()
            .currentCompletion?.closeAndFinish(true)
        CompletionStateWatcher.canceled()
    }

    fun triggerAutoComplete() {
        // NOTE: we *need* the NeojetEditor instance here since it
        //  will return the actual JComponent, unlike the EditorImpl
        //  that we have to use in other places (and which gets
        //  returned from NeojetTextFileEditor#getEditor)
        val current = CompletionServiceImpl.getCompletionService().currentCompletion
        if (current == null) {
            AutoPopupController.getInstance(project)
                .scheduleAutoPopup(panel.editor, CompletionType.SMART, null)
        } else {
            current.prefixUpdated()
        }
    }

    fun acceptExternalEdit(block: () -> Unit) {
        if (documentChangeListener.isTriggeringExternalEdit.get()) {
            return
        }

        documentChangeListener.isAcceptingExternalEdit.set(true)
        try {
            block()
        } finally {
            documentChangeListener.isAcceptingExternalEdit.set(false)
        }
    }

    fun triggerExternalEdit(block: () -> Unit) {
        documentChangeListener.isTriggeringExternalEdit.set(true)
        try {
            block()
        } finally {
            documentChangeListener.isTriggeringExternalEdit.set(false)
        }
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

private fun Neovim.highlightCmd(buffer: Buffer, type: String, highlighter: RangeHighlighterEx) {
    val tooltipObj = highlighter.errorStripeTooltip
    val description: String
    val severity: HighlightSeverity
    if (tooltipObj is HighlightInfo) {
        description = tooltipObj.description ?: ""
        severity = tooltipObj.severity
    } else {
        description = tooltipObj?.toString() ?: ""
        severity = HighlightSeverity.WARNING
    }

    if (description.isEmpty()) {
        // just ignore things without descriptions, I guess?
        return
    }

    command(StringBuilder(256).apply {
        append("call neojet#hl_")
        append(type)
        append("(")

        append(buffer.id)
        append(",")

        append(highlighter.id)
        append(",")

        append(highlighter.startOffset)
        append(",")
        append(highlighter.endOffset)
        append(",")

        append('"')
        append(description.replace("\"", "\\\""))
        append('"')

        append(",\"")
        append(severity.name)
        append("\")")

    }.toString())
        .subscribe()
}

