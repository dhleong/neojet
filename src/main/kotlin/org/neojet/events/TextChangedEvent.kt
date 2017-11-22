package org.neojet.events

import io.neovim.java.event.EventName

/**
 * @author dhleong
 */
@EventName("text_changed")
class TextChangedEvent : BufferEvent<Unit>() {
    override fun toString(): String =
        "TextChangedEvent(buf=$bufferId)"
}