package io.neovim.java.event.redraw;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.neovim.java.event.EventName;

import javax.annotation.Nonnull;

/**
 * @author dhleong
 */
@EventName("mode_change")
public class ModeChangeEvent extends RedrawSubEvent<ModeChangeEvent.ModeChangeValue> {
    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class ModeChangeValue {
        public @Nonnull String modeName = "";

        /**
         * Index into a previously-received ModeInfoSetEvent list
         */
        public int modeIndex;
    }
}
