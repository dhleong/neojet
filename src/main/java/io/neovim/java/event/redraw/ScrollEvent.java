package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;

import java.util.Collections;

/**
 * @author dhleong
 */
@EventName("scroll")
public class ScrollEvent extends RedrawSubEvent<ScrollEvent.ScrollValue> {

    // NOTE: this is wildly inefficient....
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class ScrollValue {
        public int value;

        public ScrollValue() {}
        public ScrollValue(int scrollAmount) {
            this();

            this.value = scrollAmount;
        }

            @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public ScrollEvent() {}
    public ScrollEvent(int scrollAmount) {
        this();

        value = Collections.singletonList(new ScrollValue(scrollAmount));
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
