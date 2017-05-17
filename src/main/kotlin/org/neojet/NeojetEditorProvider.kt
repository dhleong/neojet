package org.neojet

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class NeojetEditorProvider : FileEditorProvider, DumbAware {
    override fun getEditorTypeId(): String = "neojet"

    override fun accept(project: Project, file: VirtualFile): Boolean = true

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return NeojetTextFileEditor(project, file)
    }

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR

}