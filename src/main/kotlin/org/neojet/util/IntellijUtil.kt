package org.neojet.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import io.neovim.java.Buffer
import org.neojet.NVIM_BUFFER_KEY
import java.awt.Font

/**
 * @author dhleong
 */

val Document.vFile: VirtualFile
    get() = FileDocumentManager.getInstance().getFile(this)
        ?: throw IllegalArgumentException("$this didn't have a vFile")

val Editor.buffer: Buffer
    get() = getUserData(NVIM_BUFFER_KEY)
        ?: throw IllegalArgumentException("$this didn't have a Buffer")

val Editor.disposable: Disposable
    get() {
        if (this is Disposable) return this
        if (this is EditorImpl) return disposable
        throw IllegalArgumentException("$this doesn't have a Disposable")
    }

fun getEditorFont(): Font {
    // NOTE: sadly, neovim disabled the guifont option, but we can
    // respect the user's intellij settings
    var fontSize = 14
    var fontFace = Font.MONOSPACED

    EditorColorsManager.getInstance().globalScheme.let {
        fontFace = it.editorFontName
        fontSize = it.editorFontSize
    }

    return Font(fontFace, Font.PLAIN, fontSize)
}
