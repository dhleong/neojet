package org.neojet.events

import io.neovim.java.event.EventName
import io.neovim.java.rpc.NotificationPacket

/**
 * @author dhleong
 */
@EventName("test")
class TestEvent : NotificationPacket<String>() {
    override fun toString(): String = "TestEvent(${value()})"
}