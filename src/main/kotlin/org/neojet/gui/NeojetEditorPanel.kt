package org.neojet.gui

import io.neovim.java.Buffer
import io.neovim.java.event.RedrawEvent
import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.PutEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.neovim.java.event.redraw.UnknownRedrawEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.neojet.EventDispatcher
import org.neojet.HandlesEvent
import org.neojet.NJCore
import java.awt.FlowLayout
import java.util.*
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

/**
 * @author dhleong
 */

class NeojetEditorPanel(val buffer: Buffer) : JPanel(FlowLayout()) {
    val nvim = NJCore.instance.nvim!!
    val subs = CompositeDisposable()
    val dispatcher = EventDispatcher(this)

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

    fun dispose() {
        subs.clear()
    }

    internal fun dispatchRedrawEvents(events: List<RedrawSubEvent<*>>) {
        events.forEach(dispatcher::dispatch)
    }

    // TODO: actually, put these on some sort of model class
    // so we can test it

    @HandlesEvent fun cursorGoto(event: CursorGotoEvent) {
        System.out.println("GOTO: $event")
    }

    @HandlesEvent fun put(event: PutEvent) {
        System.out.println("PUT: $event")
    }

    @HandlesEvent fun onUnknown(event: UnknownRedrawEvent) =
        System.out.println("Unknown redraw event: $event")
}

