package io.neovim.java.event;

import io.neovim.java.event.redraw.RedrawSubEvent;
import io.neovim.java.rpc.NotificationPacket;

/**
 * @author dhleong
 */
@EventName("redraw")
public class RedrawEvent extends NotificationPacket<RedrawSubEvent<?>> {
}
