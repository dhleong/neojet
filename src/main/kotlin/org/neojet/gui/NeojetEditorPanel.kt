package org.neojet.gui

import io.neovim.java.event.RedrawEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.neojet.NJCore
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Graphics
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

/**
 * @author dhleong
 */

val EDITOR_ROWS_DEFAULT = 24
val EDITOR_COLS_DEFAULT = 90

class NeojetEditorPanel : JPanel(FlowLayout()) {
    val nvim = NJCore.instance.nvim!!
    val subs = CompositeDisposable()
    val uiModel = UiModel()

    var rows: Int = EDITOR_ROWS_DEFAULT
    var cols: Int = EDITOR_COLS_DEFAULT

    var isAttachedToUi: Boolean = false

    init {
        subs.add(
            nvim.notifications(RedrawEvent::class.java)
                .window(4, TimeUnit.MILLISECONDS, Schedulers.io(), 32)
                // buffer into a List, but assuming either an empty or singleton
                // list where possible to avoid unnecessary allocations

                .flatMapSingle<List<RedrawSubEvent<*>>> { it.reduce(Collections.emptyList(),
                    { list, item ->
                        if (list.isEmpty()) {
                            // was emptyList default; return the current list
                            item
                        } else {
                            // just add to the existing list
                            list.addAll(item)
                            list
                        }
                    })
                }

                .filter { it.isNotEmpty() }
                .observeOn(UiThreadScheduler.instance)
                .subscribe(this::dispatchRedrawEvents)
        )

        // TODO request desired gui font/size from nvim instance
        font = Font(Font.MONOSPACED, Font.PLAIN, 14)
    }

    override fun invalidate() {
        super.invalidate()

        val (fontWidth, fontHeight) = getFontSize()

        val rows = maxOf(1, height / fontHeight)
        val cols = maxOf(1, width / fontWidth)

        this.rows = rows
        this.cols = cols

        if (isAttachedToUi && cols > 1 && rows > 1) {
            val oldRows = uiModel.cells.rows
            val oldCols = uiModel.cells.cols
            try {
                uiModel.resize(rows, cols)
            } catch (e: Exception) {
                throw RuntimeException(
                    "Error resizing from ($oldRows x $oldCols) -> ($rows x $cols)",
                    e
                )
            }

            nvim.uiTryResize(cols, rows)
                .flatMap { nvim.command("redraw!") }
                .subscribe()
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        val (cellWidth, cellHeight) = getFontSize()
        val windowWidth = cols * cellWidth
        background = uiModel.colorBg

        g?.apply {
            color = uiModel.colorFg

//            fillRect(0, 0, windowWidth, rows * cellHeight)

            for (y in 0 until rows) {
                for (x in 0 until cols) {
                    val hasCursor = uiModel.cursorLine == y && uiModel.cursorCol == x
                    uiModel.cells[y, x].let {
                        paintCell(g, it, cellWidth, cellHeight, hasCursor)
                    }

                    translate(cellWidth, 0)
                }

                translate(-windowWidth, cellHeight)
            }
        }
    }

    fun dispose() {
        subs.clear()
    }

    internal fun getFontSize(): Pair<Int, Int> {
        val fontMetrics = getFontMetrics(font)
        val width = fontMetrics.charWidth('M')
        val height = fontMetrics.height
        return Pair(width, height)
    }

    internal fun dispatchRedrawEvents(events: List<RedrawSubEvent<*>>) {
        events.forEach(uiModel.dispatcher::dispatch)
        repaint()
    }

    fun paintCell(g: Graphics, cell: Cell, cellWidth: Int, cellHeight: Int, hasCursor: Boolean) {
        // TODO blink
        if (hasCursor) {
            g.color = uiModel.colorBg.inverted()
            g.fillRect(0, 0, cellWidth, cellHeight)

            g.color = uiModel.colorFg.inverted()
        }

        val offset = g.getFontMetrics(g.font).descent
        g.drawString(cell.value, 0, cellHeight - offset)

        if (hasCursor) {
            g.color = uiModel.colorFg
        }
    }
}

private fun Color.inverted(): Color =
    Color(255 - red, 255 - green, 255 - blue)

