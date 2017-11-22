package org.neojet.util

import io.neovim.java.Neovim
import io.neovim.java.event.RedrawEvent
import io.neovim.java.event.redraw.RedrawSubEvent
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.awt.event.KeyEvent
import java.util.concurrent.TimeUnit

/**
 * @author dhleong
 */

fun Neovim.bufferedRedrawEvents(): Flowable<List<RedrawSubEvent<*>>> {
    return listNotifications(RedrawEvent::class.java)
        .buffer(10, TimeUnit.MILLISECONDS, Schedulers.io(), 32)
//        .window(10, TimeUnit.MILLISECONDS, Schedulers.io(), 32)
        // buffer into a List, but assuming either an empty or singleton
        // list where possible to avoid unnecessary allocations

//        .flatMapSingle<List<RedrawSubEvent<*>>> { it.reduce(Collections.emptyList(),
//            { list, item ->
//                if (list.isEmpty()) {
//                    // was emptyList default; return the current list
//                    item
//                } else {
//                    // just add to the existing list
//                    list.addAll(item)
//                    list
//                }
//            })
//        }

        .filter { it.isNotEmpty() }
}

val specialKeyChars = mapOf(
    '\n' to "<CR>",
    '<' to "<LT>"
)

val specialKeyCodes = mapOf(
    37 to "<LEFT>",
    38 to "<UP>",
    39 to "<RIGHT>",
    40 to "<DOWN>"
)

// TODO tests please
// TODO special keys? modifiers?
fun KeyEvent.toVimCode() = specialKeyChars[keyChar]
    ?: specialKeyCodes[keyCode]
    ?: keyChar.toString()

fun Neovim.input(e: KeyEvent): Single<Int> {
    try {
        return input(e.toVimCode())
    } catch (e: Throwable) {
        e.printStackTrace()
        throw e
    }
}


