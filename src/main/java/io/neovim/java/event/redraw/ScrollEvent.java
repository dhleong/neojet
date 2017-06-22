package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName("scroll")
public class ScrollEvent extends RedrawSubEvent<ScrollEvent.ScrollValue> {

    // NOTE: this is wildly inefficient....
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class ScrollValue {
        public int value;

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @Override
    public String toString() {
        return "ScrollEvent{" +
            (value == null
                ? "value=null"
                : "value='" + value + '\'') +
            '}';
    }

}
