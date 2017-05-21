package org.neojet

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.ActionPlan
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import com.intellij.openapi.editor.actionSystem.TypedActionHandlerEx
import javax.swing.KeyStroke

/**
 * @author dhleong
 */

class NeovimTypedActionHandler(val original: TypedActionHandler)
    : TypedActionHandlerEx {

    override fun beforeExecute(editor: Editor, c: Char, context: DataContext, plan: ActionPlan) {
        System.out.println(
                "Before:" +
                KeyStroke.getKeyStroke(c)
        )
    }

    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
        System.out.println(
                "execute:" + KeyStroke.getKeyStroke(charTyped)
        )
    }

}
