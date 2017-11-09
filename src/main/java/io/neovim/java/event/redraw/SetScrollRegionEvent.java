package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;

import java.util.Collections;

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

        public ScrollRegion() {}
        public ScrollRegion(int left, int top, int right, int bottom) {
            this();

            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

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

    public SetScrollRegionEvent() {}
    public SetScrollRegionEvent(int left, int top, int right, int bottom) {
        this();

        value = Collections.singletonList(new ScrollRegion(
            left, top, right, bottom
        ));
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
