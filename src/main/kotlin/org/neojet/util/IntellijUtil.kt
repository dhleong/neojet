package org.neojet.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import io.neovim.java.Buffer
import io.neovim.java.IntPair
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

fun Editor.getLineEndOffset(line: Int): Int =
    logicalPositionToOffset(LogicalPosition(line + 1, 0)) - 1

fun Editor.lineRange(line: Int): IntPair {
    val thisLineStartOffset = logicalPositionToOffset(LogicalPosition(line, 0))
    val nextLineStartOffset = getLineEndOffset(line)

    return IntPair(
        thisLineStartOffset,
        nextLineStartOffset
    )
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

/**
 * @return a lambda that evaluates the given action as
 *  a WriteAction
 */
fun <T> asWriteAction(action: () -> T): () -> T {
    return {
        ApplicationManager.getApplication().runWriteAction(Computable {
            action()
        })
    }
}

fun runUndoTransparently(action: () -> Unit) {
    CommandProcessor.getInstance().runUndoTransparentAction(action)
}

/**
 * Execute the given block in a write action on the event dispatch
 *  thread, waiting for the result
 */
fun <T> inWriteAction(action: () -> T): T =
    inWriteAction(ModalityState.defaultModalityState(), action)

fun <T> inWriteAction(modality: ModalityState, action: () -> T): T {
    val wrapped = asWriteAction(action)
    val resultRef = Ref.create<T>()
    val app = ApplicationManager.getApplication()

    app.invokeAndWait({
        resultRef.set(wrapped())
    }, modality)

    return resultRef.get()
}
