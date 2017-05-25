package io.neovim.java.event;

import io.neovim.java.event.redraw.RedrawSubEvent;
import io.neovim.java.rpc.NotificationPacket;

import java.util.List;

/**
 * @author dhleong
 */
@EventName("redraw")
public class RedrawEvent extends NotificationPacket<List<RedrawSubEvent<?>>> {
    @Override
    public List<RedrawSubEvent<?>> value() {
        return args;
    }
}
