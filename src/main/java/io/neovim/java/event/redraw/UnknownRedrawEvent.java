package io.neovim.java.event.redraw;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author dhleong
 */
public class UnknownRedrawEvent extends RedrawSubEvent<JsonNode> {

//    @JsonCreator UnknownRedrawEvent(JsonNode node) {
//        value = node;
//    }

    @Override
    public String toString() {
        return "UnknownRedrawEvent{" +
            "type=" + redrawType +
            ", value=" + value +
            '}';
    }
}
