package io.neovim.java.util;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * @author dhleong
 */
public class ModeInfo {
    public enum CursorShape {
        @JsonProperty("block") BLOCK,
        @JsonProperty("horizontal") HORIZONTAL,
        @JsonProperty("vertical") VERTICAL,
    }

    @Nonnull public CursorShape cursorShape = CursorShape.BLOCK;

    /**
     * Percentage of the cell occupied by the cursor
     */
    public int cellPercentage;

    @JsonProperty("blinkoff")
    public int blinkOff;
    @JsonProperty("blinkon")
    public int blinkOn;
    @JsonProperty("blinkwait")
    public int blinkWait;

    @JsonProperty("hl_id")
    public int highlightGroupId;

    /**
     * Like {@link #highlightGroupId}, but for when langmap
     *  is active
     */
    // supposed to be hl_lm, but that's not what my nvim emits
    @JsonProperty("id_lm")
    public int highlightGroupIdLangMap;

    public int mouseShape;

    @Nonnull public String name = "";
    @Nonnull public String shortName = "";
}

