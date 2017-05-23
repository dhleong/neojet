package io.neovim.java.event.redraw;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author dhleong
 */
public class UnknownRedrawEvent extends RedrawSubEvent<UnknownRedrawEvent> {

    public JsonNode value;

//    @JsonCreator UnknownRedrawEvent(JsonNode node) {
//        value = node;
//    }

    @Override
    public String toString() {
        return "UnknownRedrawEvent{" +
            "type=" + type +
            ", value=" + value +
            '}';
    }
}
