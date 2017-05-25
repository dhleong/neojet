package io.neovim.java.event.redraw;

import com.fasterxml.jackson.databind.JsonNode;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName("clear")
public class ClearScreenEvent extends RedrawSubEvent<JsonNode> {
}
