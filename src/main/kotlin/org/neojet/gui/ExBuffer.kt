package org.neojet.gui

/**
 * @author dhleong
 */
class ExBuffer {

    private val lines = mutableListOf<StringBuilder>()

    var isActive: Boolean = false

    fun append(line: CharSequence) {
        isActive = true
        lines.add(StringBuilder(line))
    }

    fun clear() {
        lines.clear()
        isActive = false
    }

    fun deleteAfter(col: Int) {
        if (lines.isEmpty()) {
            return
        }

        lines.last().let {
            it.delete(col, it.length)
        }
    }

    fun put(col: Int, lineText: CharSequence) {
        if (lines.isEmpty()) {
            append(" ".repeat(col))
        }

        lines.last().let {
            val diff = col - it.length
            if (diff > 0) {
                it.append(" ".repeat(diff))
            }

            it.insert(col, lineText)
        }
        isActive = true
    }

    fun getLines() = lines
}