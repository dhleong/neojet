package org.neojet.gui

import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.EolClearEvent
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

data class Cell(var value: String) {
    fun copyFrom(other: Cell) {
        value = other.value
    }
}

class UiModel {

    val dispatcher: EventDispatcher by lazy { EventDispatcher(this) }

    var colorFg: Color = Color.BLACK
    var colorBg: Color = Color.WHITE
    var colorSp: Color = Color.BLUE

    // NOTE: lines are 1-indexed in nvim
    var cells = Matrix(EDITOR_ROWS_DEFAULT, EDITOR_COLS_DEFAULT, this::createEmptyCell)

    var cursorLine: Int = 0
    var cursorCol: Int = 0

    private var currentScrollRegion = SetScrollRegionEvent.ScrollRegion()

    fun resize(rows: Int, cols: Int) {
        cells = cells.resizeTo(rows, cols, this::createEmptyCell)
    }

    @HandlesEvent fun clearToEol(event: EolClearEvent) {
        event.value.forEach {
            clearToEol(cursorLine, cursorCol)
        }
    }

    private fun clearToEol(line: Int, startCol: Int) {
        for (col in startCol until cells.cols) {
            cells[line, col].value = " "
        }
    }

    @HandlesEvent fun cursorGoto(event: CursorGotoEvent) {
        // NOTE: there's only ever one
        cursorLine = event.value[0].row()
        cursorCol = event.value[0].col()

        if (cursorLine >= cells.rows) {
            System.err.println(
                "WARN: GOTO($event) with cells: (${cells.rows} x ${cells.cols})")
        }
    }

    @HandlesEvent fun onUnknown(event: UnknownRedrawEvent) =
        System.out.println("Unknown redraw event: $event")

    @HandlesEvent fun put(event: PutEvent) {
        if (cursorLine >= cells.rows) {
            System.err.println("WARN: unusable put on line $cursorLine vs ${cells.rows}")
            return
        }

//        System.out.println("PUT: $event @($cursorLine, $cursorCol)")
        event.value.forEach {
            if (cursorCol < cells.cols) {
                cells[cursorLine, cursorCol].value = it.value.toString()
                ++cursorCol
            }
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
            "update_fg" -> colorFg = newColor
            "update_bg" -> colorBg = newColor
            "update_sp" -> colorSp = newColor
        }
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
            cells[to, i].copyFrom(cells[from, i])
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun createEmptyCell(row: Int, col: Int): Cell = Cell(" ")
}
