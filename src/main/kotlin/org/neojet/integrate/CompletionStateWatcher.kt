package org.neojet.integrate

import com.intellij.codeInsight.completion.CompletionPhase.NoCompletion
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl
import org.neojet.NJCore
import org.neojet.gui.UiThreadScheduler
import java.util.concurrent.TimeUnit

/**
 * @author dhleong
 */
object CompletionStateWatcher {

    var isCompletionShown: Boolean = false
        private set

    fun canceled() {
        checkCompletionChange(false)
    }

    fun onKeyTyped() {
        if (!checkCompletion() && !isCompletionShown) {
            // it's possible it will be shown shortly... check later
            UiThreadScheduler.instance.scheduleDirect(50, TimeUnit.MILLISECONDS) {
                checkCompletion()
            }
        }
    }

    private fun checkCompletion(): Boolean {
        val hasCompletion = CompletionServiceImpl.getCompletionPhase() != NoCompletion
        return checkCompletionChange(hasCompletion)
    }

    private fun checkCompletionChange(newState: Boolean): Boolean {
        if (isCompletionShown != newState) {
            onCompletionStateChanged(newState)
            return true
        }

        // no change
        return false
    }

    private fun onCompletionStateChanged(isNowShown: Boolean) {
        isCompletionShown = isNowShown

        // notify nvim
        System.out.println("completionStateChanged: $isNowShown")
        val isShownNumber =
            if (isNowShown) 1
            else 0
        NJCore.instance.nvim?.command("call neojet#completionState($isShownNumber)")
    }

}