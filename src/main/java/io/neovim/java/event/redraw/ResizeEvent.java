package io.neovim.java.event.redraw;

import io.neovim.java.IntPair;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName("resize")
public class ResizeEvent extends RedrawSubEvent<IntPair> {
}
