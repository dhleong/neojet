package org.neojet.gui

import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.PutEvent
import io.neovim.java.event.redraw.UnknownRedrawEvent
import io.neovim.java.event.redraw.UpdateColorEvent
import org.neojet.EventDispatcher
import org.neojet.HandlesEvent
import java.awt.Color

/**
 * @author dhleong
 */

class UiModel {

    val dispatcher: EventDispatcher by lazy { EventDispatcher(this) }

    var colorFg: Color = Color.BLACK
    var colorBg: Color = Color.WHITE
    var colorSp: Color = Color.BLUE

    @HandlesEvent fun cursorGoto(event: CursorGotoEvent) {
        System.out.println("GOTO: $event")
    }

    @HandlesEvent fun onUnknown(event: UnknownRedrawEvent) =
        System.out.println("Unknown redraw event: $event")

    @HandlesEvent fun put(event: PutEvent) {
        System.out.println("PUT: $event")
    }

    @HandlesEvent fun updateColor(event: UpdateColorEvent) {
        // there's only ever one value
        val newColor = Color(event.value[0].color)
        when (event.redrawType) {
            "update_fg" -> colorFg = newColor
            "update_bg" -> colorBg = newColor
            "update_sp" -> colorSp = newColor
        }
    }

}
