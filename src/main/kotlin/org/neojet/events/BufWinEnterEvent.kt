package org.neojet.events

import io.neovim.java.event.EventName
import io.neovim.java.rpc.NotificationPacket

/**
 * @author dhleong
 */
@EventName("buf_win_enter")
class BufWinEnterEvent : NotificationPacket<String>() {

    val path: String
        get() = value().first()

    override fun toString(): String = "BufWinEnterEvent(${value()})"
}