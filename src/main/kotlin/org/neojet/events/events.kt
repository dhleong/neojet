package org.neojet.events

import io.neovim.java.Neovim

/**
 * @author dhleong
 */
val eventTypes = arrayOf(
    BufWinEnterEvent::class.java
)

fun Neovim.registerCustomEvents(): Neovim {
    eventTypes.forEach { type ->
        registerEventType(type)
    }

    return this
}