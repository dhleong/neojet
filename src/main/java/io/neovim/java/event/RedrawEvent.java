package io.neovim.java.event;

import io.neovim.java.event.redraw.RedrawSubEvent;
import io.neovim.java.rpc.NotificationListPacket;

/**
 * @author dhleong
 */
@EventName("redraw")
public class RedrawEvent extends NotificationListPacket<RedrawSubEvent<?>> {
}
