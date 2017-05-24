package org.neojet.gui

import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.PutEvent
import io.neovim.java.event.redraw.UnknownRedrawEvent
import io.neovim.java.event.redraw.UpdateColorEvent
import org.neojet.EventDispatcher
import org.neojet.HandlesEvent
import org.neojet.util.Matrix
import java.awt.Color

/**
 * @author dhleong
 */

data class Cell(var value: String)

class UiModel {

    val dispatcher: EventDispatcher by lazy { EventDispatcher(this) }

    var colorFg: Color = Color.BLACK
    var colorBg: Color = Color.WHITE
    var colorSp: Color = Color.BLUE

    // NOTE: lines are 1-index in nvim
    var cells = Matrix<Cell>(EDITOR_ROWS_DEFAULT, EDITOR_COLS_DEFAULT, this::createEmptyCell)

    var cursorLine: Int = 0
    var cursorCol: Int = 0

    fun resize(rows: Int, cols: Int) {
        cells = cells.resizeTo(rows, cols, this::createEmptyCell)
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

    @HandlesEvent fun updateColor(event: UpdateColorEvent) {
        // NOTE: there's only ever one
        val newColor = Color(event.value[0].color)
        when (event.redrawType) {
            "update_fg" -> colorFg = newColor
            "update_bg" -> colorBg = newColor
            "update_sp" -> colorSp = newColor
        }
    }

    private fun createEmptyCell(row: Int, col: Int): Cell = Cell(" ")
}
