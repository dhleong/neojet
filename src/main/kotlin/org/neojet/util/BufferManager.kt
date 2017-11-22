package org.neojet.util

import io.neovim.java.Buffer
import org.neojet.NeojetTextFileEditor

/**
 * @author dhleong
 */
class BufferManager {

    private val textFileEditors = mutableMapOf<Long, NeojetTextFileEditor>()
    private val buffersById = mutableMapOf<Long, Buffer>()

    operator fun get(bufferId: Long) = buffersById[bufferId]

    fun textFileEditorForBuffer(buffer: Buffer) = textFileEditors[buffer.id]

    fun textFileEditorForBuffer(buffer: Long) = textFileEditors[buffer]

    fun add(buf: Buffer, textFileEditor: NeojetTextFileEditor) {
        buffersById[buf.id] = buf
        textFileEditors[buf.id] = textFileEditor
    }
}