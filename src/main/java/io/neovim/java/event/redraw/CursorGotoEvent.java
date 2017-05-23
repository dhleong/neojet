package io.neovim.java.event.redraw;

import io.neovim.java.IntPair;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName("cursor_goto")
public class CursorGotoEvent extends RedrawSubEvent<CursorGotoEvent> {
//    public int row, col;
    public IntPair cursor;
}
