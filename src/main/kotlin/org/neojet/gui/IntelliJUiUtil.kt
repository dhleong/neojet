package org.neojet.gui

import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager



/**
 * @author dhleong
 */
fun showStatusMessage(message: String) {
    val wm = WindowManager.getInstance()
    ProjectManager.getInstance().openProjects.forEach {
        wm.getStatusBar(it)?.info = message
    }
}

