package org.neojet.gui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.ShortcutSet
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Key
import org.neojet.NeojetTextFileEditor
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import javax.swing.KeyStroke

/**
 * Some keystrokes can't just be handled by the KeyListener
 *  on the Panel, and have to be registered as shortcuts
 *  in order for us to receive them.
 *
 * @author dhleong
 */
class NeojetShortcutKeyAction(
    private val editor: NeojetTextFileEditor
) : AnAction(), DumbAware {

    companion object {
        private val dataKey = Key<NeojetShortcutKeyAction>("org.neojet.NeojetShortcutKeyAction")

        private val strokes = listOf(
            KeyStroke.getKeyStroke(VK_N, KeyEvent.CTRL_DOWN_MASK),

            KeyStroke.getKeyStroke(VK_UP, 0),
            KeyStroke.getKeyStroke(VK_RIGHT, 0),
            KeyStroke.getKeyStroke(VK_DOWN, 0),
            KeyStroke.getKeyStroke(VK_LEFT, 0)
        )

        fun install(editor: NeojetTextFileEditor) {

            val action = NeojetShortcutKeyAction(editor).also {
                editor.putUserData(dataKey, it)
            }

            val shortcutSet: ShortcutSet = CustomShortcutSet(
                *strokes.map { KeyboardShortcut(it, null) }.toTypedArray()
            )
            action.registerCustomShortcutSet(shortcutSet, editor.component)
        }

        fun uninstall(editor: NeojetTextFileEditor) {
            editor.getUserData(dataKey)
                ?.unregisterCustomShortcutSet(editor.component)
        }
    }

    override fun actionPerformed(e: AnActionEvent) {

        // TODO we may want to check for conflicts...
        //  but for now, just pass along to the editor

        e.keyEvent?.let { ev ->
//            editor.nvim.input(it).subscribe()
            editor.panel.keyListeners.forEach {
                it.keyTyped(ev)
            }
        }
    }

}

private val AnActionEvent.keyEvent: KeyEvent?
    get() = inputEvent as? KeyEvent

