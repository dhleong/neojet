package org.neojet.events

import io.neovim.java.Neovim
import io.neovim.java.rpc.NotificationPacket

/**
 * @author dhleong
 */
abstract class BufferEvent<out T> : NotificationPacket<Any>() {
    val bufferId: Long
        get() = (value()[0] as Int).toLong()

    @Suppress("UNCHECKED_CAST")
    val arg: T
        get() = value()[1] as T
}

val eventTypes = arrayOf(
    BufWinEnterEvent::class.java,
    TextChangedEvent::class.java
)

fun Neovim.registerCustomEvents() = this.also {
    eventTypes.forEach { type ->
        registerEventType(type)
    }
}