package io.neovim.java.event.redraw;

import com.fasterxml.jackson.databind.JsonNode;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName("eol_clear")
public class EolClearEvent extends RedrawSubEvent<JsonNode> {
}
