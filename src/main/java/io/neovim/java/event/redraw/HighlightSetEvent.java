package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName("highlight_set")
public class HighlightSetEvent extends RedrawSubEvent<HighlightSetEvent.HighlightWrapper> {

    // NOTE: this is wildly inefficient....
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class HighlightWrapper {
        public HighlightValue value;
    }

    public static class HighlightValue {
        public Integer foreground;
        public Integer background;
        public Integer special;

        public Boolean reverse;
        public Boolean italic;
        public Boolean bold;
        public Boolean underline;
        public Boolean undercurl;

        /**
         * @return True if no values are provided
         */
        public boolean isEmpty() {
            return foreground == null
                && background == null
                && special == null
                && reverse == null
                && italic == null
                && bold == null
                && underline == null
                && undercurl == null;
        }
    }
}
