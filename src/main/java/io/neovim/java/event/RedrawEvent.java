package io.neovim.java.event;

import io.neovim.java.event.redraw.RedrawSubEvent;
import io.neovim.java.rpc.NotificationPacket;

import java.util.List;

/**
 * @author dhleong
 */
@EventName("redraw")
public class RedrawEvent extends NotificationPacket<List<RedrawSubEvent<?>>> {
    public List<RedrawSubEvent<?>> subEvents;

    @Override
    public List<RedrawSubEvent<?>> value() {
        return subEvents;
    }
}
