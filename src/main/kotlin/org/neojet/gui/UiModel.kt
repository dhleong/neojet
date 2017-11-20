package org.neojet.gui

import io.neovim.java.event.redraw.ClearScreenEvent
import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.EolClearEvent
import io.neovim.java.event.redraw.HighlightSetEvent
import io.neovim.java.event.redraw.PutEvent
import io.neovim.java.event.redraw.ScrollEvent
import io.neovim.java.event.redraw.SetScrollRegionEvent
import io.neovim.java.event.redraw.UnknownRedrawEvent
import io.neovim.java.event.redraw.UpdateColorEvent
import org.neojet.EventDispatcher
import org.neojet.HandlesEvent
import org.neojet.util.Matrix
import java.awt.Color

/**
 * @author dhleong
 */

data class Cell(
    var value: String,
    var attrs: CellAttributes = CellAttributes()
) {
    fun setFrom(other: Cell) {
        value = other.value
        attrs = other.attrs
    }
}

data class CellAttributes(
    var fg: Color = Color.BLACK,
    var bg: Color = Color.WHITE,
    var sp: Color = Color.BLUE,

    var reverse: Boolean = false,
    var italic: Boolean = false,
    var bold: Boolean = false,
    var underline: Boolean = false,
    var undercurl: Boolean = false,

    var isDefault: Boolean = true
) {

    fun setFrom(value: HighlightSetEvent.HighlightValue, defaults: CellAttributes) {
        if (value.isEmpty) {
            resetToDefaults(defaults)
            return
        }

        fg = value.foreground?.toColor() ?: defaults.fg
        bg = value.background?.toColor() ?: defaults.bg
        sp = value.special?.toColor() ?: defaults.sp

        reverse = value.bold ?: defaults.bold
        italic = value.italic ?: defaults.italic
        bold = value.bold ?: defaults.bold
        underline = value.underline ?: defaults.underline
        undercurl = value.undercurl ?: defaults.undercurl
    }

    fun setFrom(other: CellAttributes) {
        fg = other.fg
        bg = other.bg
        sp = other.sp

        reverse = other.bold
        italic = other.italic
        bold = other.bold
        underline = other.underline
        undercurl = other.undercurl

        isDefault = false
    }

    fun resetToDefaults(defaultAttrs: CellAttributes) {
        setFrom(defaultAttrs)
        isDefault = true
    }
}

class UiModel {

    val dispatcher: EventDispatcher by lazy { EventDispatcher(this) }

    // NOTE: lines are 1-indexed in nvim
    var cells = Matrix(EDITOR_ROWS_DEFAULT, EDITOR_COLS_DEFAULT, this::createEmptyCell)

    var cursorLine: Int = 0
    var cursorCol: Int = 0

    private var currentScrollRegion = SetScrollRegionEvent.ScrollRegion()
    private val defaultAttrs = CellAttributes()
    private val currentAttrs = CellAttributes()

    fun resize(rows: Int, cols: Int) {
        cells = cells.resizeTo(rows, cols, this::createEmptyCell)
    }

    @HandlesEvent fun clearScreen(event: ClearScreenEvent) {
        for (line in 0 until cells.rows) {
            clearToEol(line, 0)
        }

        cursorCol = 0
        cursorLine = 0
    }

    @HandlesEvent fun clearToEol(event: EolClearEvent) {
        event.value.forEach {
            clearToEol(cursorLine, cursorCol)
        }
    }

    private fun clearToEol(line: Int, startCol: Int) {
        for (col in startCol until cells.cols) {
            cells[line, col].apply {
                value = " "
                attrs.setFrom(currentAttrs)
            }
        }
    }

    @HandlesEvent fun cursorGoto(event: CursorGotoEvent) {
        // NOTE: there's only ever one
        cursorLine = event.value[0].row()
        cursorCol = event.value[0].col()
    }

    @HandlesEvent fun setHighlight(event: HighlightSetEvent) {
        for (ev in event.value) {
            currentAttrs.setFrom(ev.value, defaults = defaultAttrs)
        }
    }

    @HandlesEvent fun onUnknown(event: UnknownRedrawEvent) =
        System.out.println("Unknown redraw event: $event")

    @HandlesEvent fun put(event: PutEvent) {
        event.value.forEach {
            cells[cursorLine, cursorCol].apply {
                value = it.value.toString()
                attrs.setFrom(currentAttrs)
            }
            ++cursorCol
        }
    }

    @HandlesEvent fun scroll(event: ScrollEvent) {

        val region = currentScrollRegion
        for (scroll in event.value) {
            val scrollAmount = scroll.value

            val clearTop: Int
            val clearBot: Int

            val lineRange = region.left..region.right
            if (scrollAmount > 0) {
                // scrolling up; clear lines below
                clearTop = region.bottom - scrollAmount + 1
                clearBot = region.bottom

                var dst = region.top
                for (line in (region.top + scrollAmount)..region.bottom) {
                    copyLineTo(line, dst, lineRange)
                    ++dst
                }
            } else {
                // scrolling down; clear lines above
                clearTop = region.top
                clearBot = region.top - scrollAmount - 1

                var dst = region.bottom
                for (line in (region.bottom + scrollAmount) downTo region.top) {
                    copyLineTo(line, dst, lineRange)
                    --dst
                }
            }

            for (line in clearTop..clearBot) {
                clearToEol(line, 0)
            }
        }
    }

    @HandlesEvent fun setScrollRegion(event: SetScrollRegionEvent) {
        currentScrollRegion = event.value.last()
    }

    @HandlesEvent fun updateColor(event: UpdateColorEvent) {
        // NOTE: there's only ever one
        val newColor = Color(event.value[0].color)
        when (event.redrawType) {
            "update_fg" -> defaultAttrs.fg = newColor
            "update_bg" -> defaultAttrs.bg = newColor
            "update_sp" -> defaultAttrs.sp = newColor
        }

        cells.filter { it.attrs.isDefault }
            .forEach { it.attrs.resetToDefaults(defaultAttrs) }
    }

    /**
     * Utility for testing, mostly
     */
    fun getContents(trimEnd: Boolean = true): String = StringBuilder().also { builder ->
        val lineBuilder = StringBuilder(cells.cols)
        var first = true

        for (line in 0 until cells.rows) {

            if (!first) builder.append('\n')
            first = false

            lineBuilder.setLength(0)
            for (col in 0 until cells.cols) {
                lineBuilder.append(cells[line, col].value)
            }

            if (trimEnd) {
                builder.append(lineBuilder.trim())
            } else {
                builder.append(lineBuilder)
            }
        }
    }.toString()

    /**
     * Utility for testing, mostly
     */
    fun setContents(contents: String) {
        for ((line, textLine) in contents.split("\n").withIndex()) {
            for (i in textLine.indices) {
                cells[line, i].value = textLine[i].toString()
            }
        }
    }

    private fun copyLineTo(from: Int, to: Int, colsRange: IntRange) {
        for (i in colsRange) {
            cells[to, i].setFrom(cells[from, i])
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun createEmptyCell(row: Int, col: Int): Cell = Cell(" ")
}

private fun Int.toColor(): Color = Color(this)
