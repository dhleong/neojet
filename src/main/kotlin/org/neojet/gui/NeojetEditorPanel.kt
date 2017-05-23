package org.neojet.gui

import io.neovim.java.Buffer
import io.neovim.java.event.RedrawEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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

    init {
        subs.add(
            nvim.notifications(RedrawEvent::class.java)
                .window(4, TimeUnit.MILLISECONDS, Schedulers.io(), 32)
                // buffer into a List, but assuming either an empty or singleton
                // list where possible to avoid unnecessary allocations

                .flatMapSingle<List<List<RedrawSubEvent<*>>>> { it.reduce(Collections.emptyList(),
                    { list, item ->
                        if (list.isEmpty()) {
                            Collections.singletonList(item)
                        } else if (list.size == 1) {
                            // it was a singletonList; make it a proper ArrayList
                            val result = ArrayList<List<RedrawSubEvent<*>>>()
                            result.add(item)
                            result
                        } else {
                            // just add to the existing list
                            list.add(item)
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

    internal fun dispatchRedrawEvents(events: List<List<RedrawSubEvent<*>>>) {
        events.forEach(this::dispatchRedrawEvent)
    }

    internal fun dispatchRedrawEvent(event: List<RedrawSubEvent<*>>) {
//        System.out.println("Dispatch: ${event[0]}) : ${event[1]}")
        System.out.println("Dispatch: " + event)
    }

}

