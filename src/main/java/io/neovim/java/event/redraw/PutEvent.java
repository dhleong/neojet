package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName("put")
public class PutEvent extends RedrawSubEvent<PutEvent.PutValue> {

    // NOTE: this is wildly inefficient....
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class PutValue {
        public char value;

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @Override
    public String toString() {
        return "PutEvent{" +
            (value == null
                ? "value=null"
                : "value='" + value + '\'') +
            '}';
    }

}


