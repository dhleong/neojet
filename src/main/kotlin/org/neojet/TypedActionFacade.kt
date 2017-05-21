package org.neojet

import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.actionSystem.TypedActionHandler

/**
 * @author dhleong
 */

class TypedActionFacade {
    companion object {
        val Instance: TypedActionFacade by lazy { TypedActionFacade() }
    }

    private var originalTypedActionHandler: TypedActionHandler? = null

    fun installHandler(factory: (TypedActionHandler) -> TypedActionHandler) {
        val typedAction = getTypedAction()
        originalTypedActionHandler = typedAction.handler
        typedAction.setupRawHandler(factory.invoke(typedAction.handler))
    }
    fun installHandler(handler: TypedActionHandler) {
        installHandler { _ -> handler}
    }

    fun restoreHandler() {
        originalTypedActionHandler?.let {
            getTypedAction().setupRawHandler(it)
            originalTypedActionHandler = null
        }
    }

    private fun getTypedAction(): TypedAction {
        return EditorActionManager.getInstance().typedAction
    }
}
