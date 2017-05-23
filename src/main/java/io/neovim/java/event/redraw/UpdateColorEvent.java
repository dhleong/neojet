package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;

/**
 * @author dhleong
 */
@EventName({"update_fg", "update_bg", "update_sp"})
public class UpdateColorEvent extends RedrawSubEvent<UpdateColorEvent.ColorData> {
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class ColorData {
        public int color;

        @Override
        public String toString() {
            return Integer.toHexString(color);
        }
    }
}
