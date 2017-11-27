package org.neojet.gui

import java.util.concurrent.TimeUnit
import javax.swing.JComponent

/**
 * A very insistent way to take focus
 * @author dhleong
 */
fun JComponent.demandFocus(attempts: Int = 10) {
    if (!isFocusOwner && attempts > 0) {
        requestFocusInWindow()

        UiThreadScheduler.instance.scheduleDirect(100, TimeUnit.MILLISECONDS) {
            demandFocus(attempts - 1)
        }
    }
}
