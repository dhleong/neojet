package org.neojet.gui

import io.neovim.java.event.RedrawEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.neojet.NJCore
import java.awt.FlowLayout
import java.awt.Graphics
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

/**
 * @author dhleong
 */

class NeojetEditorPanel : JPanel(FlowLayout()) {
    val nvim = NJCore.instance.nvim!!
    val subs = CompositeDisposable()
    val uiModel = UiModel()

    var rows: Int = 90 // default value
    var cols: Int = 24 // default value

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
    }

    override fun invalidate() {
        super.invalidate()

        val (fontWidth, fontHeight) = getFontSize()

        val rows = maxOf(1, height / fontHeight)
        val cols = maxOf(1, width / fontWidth)

        this.rows = rows
        this.cols = cols

        if (isAttachedToUi) {
            System.out.println("Invalidate $rows x $cols")
            nvim.uiTryResize(cols, rows)
                .flatMap { nvim.command("redraw!") }
                .subscribe()
        }
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)

        val (cellWidth, cellHeight) = getFontSize()

        g?.apply {
            background = uiModel.colorBg
            color = uiModel.colorFg

            drawRect(0, 0, width * cellWidth, height * cellHeight)
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
}

