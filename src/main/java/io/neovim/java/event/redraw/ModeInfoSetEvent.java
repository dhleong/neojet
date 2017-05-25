package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;
import io.neovim.java.util.ModeInfo;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author dhleong
 */
@EventName("mode_info_set")
public class ModeInfoSetEvent extends RedrawSubEvent<ModeInfoSetEvent.ModeInfoSetValue> {

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class ModeInfoSetValue {
        public boolean isCursorStyleEnabled;

        public @Nonnull List<ModeInfo> modes = Collections.emptyList();

        @Override
        public String toString() {
            return isCursorStyleEnabled
                + ", "
                + modes;
        }
    }

}
