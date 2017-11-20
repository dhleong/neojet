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
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.font.TextAttribute
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
                .observeOn(UiThreadScheduler.instance)
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

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        (g as Graphics2D).apply {

            val awtHints =
                Toolkit.getDefaultToolkit()
                    .getDesktopProperty("awt.font.desktophints")
                    as Map<*, *>?

            if (awtHints == null) {
                // no platform-provided hints... do our best
                setRenderingHints(mapOf(
                    RenderingHints.KEY_TEXT_ANTIALIASING
                        to RenderingHints.VALUE_TEXT_ANTIALIAS_GASP,

                    RenderingHints.KEY_RENDERING
                        to RenderingHints.VALUE_RENDER_QUALITY
                ))
            } else {
                // the platform is providing appropriate rendering hints---use 'em!
                setRenderingHints(awtHints)
            }

            // we could probably create two separate Graphics objects
            // and composite them together, so we only need one loop
            // to render everything...
            forEachCell(g) { y, x, cellWidth, cellHeight ->
                val hasCursor = uiModel.cursorLine == y && uiModel.cursorCol == x
                uiModel.cells[y, x].let {
                    paintCellBg(g, it, cellWidth, cellHeight, hasCursor)
                }
            }

            forEachCell(g) { y, x, _, cellHeight ->
                val hasCursor = uiModel.cursorLine == y && uiModel.cursorCol == x
                uiModel.cells[y, x].let {
                    paintCellFg(g, it, cellHeight, hasCursor)
                }
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

    internal fun paintCellBg(g: Graphics, cell: Cell, cellWidth: Int, cellHeight: Int, hasCursor: Boolean) {
        // TODO blink cursor? insert mode cursor?
        if (hasCursor) {
            g.color = cell.attrs.bg.inverted()
        } else {
            g.color = cell.attrs.bg
        }

        g.fillRect(0, 0, cellWidth, cellHeight)
    }

    internal fun paintCellFg(g: Graphics, cell: Cell, cellHeight: Int, hasCursor: Boolean) {
        if (hasCursor) {
            g.color = cell.attrs.fg.inverted()
        } else {
            g.color = cell.attrs.fg
        }

        g.font = getFontFor(cell.attrs)
        val offset = g.getFontMetrics(g.font).descent
        g.drawString(cell.value, 0, cellHeight - offset)
    }

    private fun getFontFor(attrs: CellAttributes): Font {
        // TODO cache this
//        val style = (
//            if (attrs.bold) Font.BOLD
//            else 0
//        ) or (
//            if (attrs.italic) Font.ITALIC
//            else 0
//        )
//
//        val base = getEditorFont(style)

        return font.deriveFont(mapOf<TextAttribute, Any?>(
            TextAttribute.WEIGHT to
                if (attrs.bold) TextAttribute.WEIGHT_BOLD
                else null,

            TextAttribute.UNDERLINE to
                if (attrs.underline) TextAttribute.UNDERLINE_ON
                else null
        ))
    }

    private fun updateFont() {
        font = getEditorFont()
    }

    private inline fun forEachCell(
        g: Graphics2D,
        block: (y: Int, x: Int, cellWidth: Int, cellHeight: Int) -> Unit
    ) {

        val (cellWidth, cellHeight) = getFontSize()
        val windowWidth = cols * cellWidth

        val oldTransform = g.transform

        for (y in 0 until rows) {
            for (x in 0 until cols) {
                block(y, x, cellWidth, cellHeight)

                g.translate(cellWidth, 0)
            }

            g.translate(-windowWidth, cellHeight)
        }

        g.transform = oldTransform
    }

}

private fun Color.inverted(): Color =
    Color(255 - red, 255 - green, 255 - blue)

