package org.neojet.util

import io.neovim.java.Neovim
import io.neovim.java.event.RedrawEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.neojet.gui.UiThreadScheduler
import java.awt.event.KeyEvent
import java.util.Collections
import java.util.concurrent.TimeUnit

/**
 * @author dhleong
 */

fun Neovim.bufferedRedrawEvents(): Flowable<List<RedrawSubEvent<*>>> {
    return notifications(RedrawEvent::class.java)
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
}

// TODO special keys? modifiers?
fun Neovim.input(e: KeyEvent): Single<Int> =
    input(e.keyChar.toString())


