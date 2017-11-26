package org.neojet.events

import io.neovim.java.event.EventName

/**
 * @author dhleong
 */
@EventName("text_changed")
class TextChangedEvent : BufferEvent<TextChangedEvent.Change>() {

    data class Change(
        var type: String = "",
        val mod: Boolean = false,
        var start: Int = 0,
        var end: Int = 0,
        var text: String = ""
    ) {

        // NOTE: when in range change mode,
        //  the text field is reused for flags
        val hasBufWritePostFlag: Boolean
            get() = text == "BufWritePost"

    }

}