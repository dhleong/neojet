package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName("set_scroll_region")
public class SetScrollRegionEvent extends RedrawSubEvent<SetScrollRegionEvent.ScrollRegion> {

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class ScrollRegion {
        public int top;
        public int bottom;
        public int left;
        public int right;

        @Override
        public String toString() {
            return "ScrollRegion{" +
                "top=" + top +
                ", bottom=" + bottom +
                ", left=" + left +
                ", right=" + right +
                '}';
        }
    }

    @Override
    public String toString() {
        return "SetScrollRegionEvent{" +
            (value == null
                ? "value=null"
                : "value='" + value + '\'') +
            '}';
    }
}
