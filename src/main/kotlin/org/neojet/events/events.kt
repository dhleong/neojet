package org.neojet.events

import com.fasterxml.jackson.annotation.JsonFormat
import io.neovim.java.Neovim
import io.neovim.java.rpc.NotificationPacket

/**
 * @author dhleong
 */
abstract class BufferEvent<T> : NotificationPacket<BufferEvent.BufferEventArg<T>>() {
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    class BufferEventArg<T> {
        var bufferId: Int = -1
        var cursorOffset: Int = 0

        var arg: T? = null
    }

    val bufferId: Long
        get() = value().bufferId.toLong()

    @Suppress("UNCHECKED_CAST")
    val arg: T
        get() = value().arg!!
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