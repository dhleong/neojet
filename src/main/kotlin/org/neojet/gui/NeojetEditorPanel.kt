package org.neojet.gui

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.Disposer
import com.intellij.util.messages.MessageBusConnection
import io.neovim.java.event.redraw.RedrawSubEvent
import io.reactivex.disposables.CompositeDisposable
import org.neojet.NJCore
import org.neojet.util.bufferedRedrawEvents
import org.neojet.util.getEditorFont
import org.neojet.util.input
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

/**
 * @author dhleong
 */

val EDITOR_ROWS_DEFAULT = 24
val EDITOR_COLS_DEFAULT = 90

class NeojetEditorPanel : JPanel(FlowLayout()), Disposable {
    val nvim = NJCore.instance.nvim!!
    val subs = CompositeDisposable()
    val uiModel = UiModel()

    var rows: Int = EDITOR_ROWS_DEFAULT
    var cols: Int = EDITOR_COLS_DEFAULT

    var isAttachedToUi: Boolean = false

    private val messageBusConnection: MessageBusConnection

    init {
        subs.addAll(
            nvim.bufferedRedrawEvents()
                .subscribe(this::dispatchRedrawEvents)
        )

        // listen for font changes so we can resize ourselves
        messageBusConnection =
            ApplicationManager.getApplication()
                .messageBus
                .connect(this)

        messageBusConnection.subscribe(EditorColorsManager.TOPIC, EditorColorsListener {
            updateFont()
            repaint()
        })

        // initial font config
        updateFont()

        // handle keyboard events
        focusTraversalKeysEnabled = false
        isFocusable = true
        requestFocus()
        addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent?) {
                e?.let {
                    nvim.input(it).subscribe()
                }
            }
        })

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                requestFocus()
                // TODO move cursor?
            }
        })
    }

    override fun dispose() {
        subs.clear()

        Disposer.dispose(this)
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

        (g as Graphics2D).apply {
            setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP
            )

            color = uiModel.colorFg

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

    internal fun paintCell(g: Graphics, cell: Cell, cellWidth: Int, cellHeight: Int, hasCursor: Boolean) {
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

    private fun updateFont() {
        font = getEditorFont()
    }

}

private fun Color.inverted(): Color =
    Color(255 - red, 255 - green, 255 - blue)

