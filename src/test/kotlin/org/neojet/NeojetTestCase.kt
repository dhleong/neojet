package org.neojet

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import java.awt.event.KeyEvent
import javax.swing.KeyStroke


/**
 * @author dhleong
 */
abstract class NeojetTestCase : UsefulTestCase() {
    private lateinit var myFixture: CodeInsightTestFixture

    private val testDataPath: String
        get() = PathManager.getHomePath() + "/community/plugins/neojet/testData"

    protected lateinit var facade: NeojetEnhancedEditorFacade

    override fun setUp() {
        super.setUp()

        NJCore.isTestMode = true

        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val projectDescriptor = LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR
        val fixtureBuilder = factory.createLightFixtureBuilder(projectDescriptor)
        val fixture = fixtureBuilder.fixture
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture,
            LightTempDirTestFixtureImpl(true)).also {

            it.setUp()
            it.testDataPath = testDataPath
        }
    }

    override fun tearDown() {
        myFixture.tearDown()
        facade.dispose()

        NJCore.isTestMode = false

        super.tearDown()
    }

    protected fun typeTextInFile(keys: List<KeyStroke>, fileContents: String): Editor {
        configureByText(fileContents)
        return typeText(keys)
    }

    @Suppress("MemberVisibilityCanPrivate")
    protected fun configureByText(
        content: String,
        fileType: LanguageFileType = PlainTextFileType.INSTANCE
    ): Editor {
        myFixture.configureByText(fileType, content)
        facade = NeojetEnhancedEditorFacade.install(myFixture.editor)
        return myFixture.editor
    }

    protected fun configureByJavaText(content: String) =
        configureByText(content, JavaFileType.INSTANCE)

    protected fun configureByXmlText(content: String) =
        configureByText(content, XmlFileType.INSTANCE)

    @Suppress("MemberVisibilityCanPrivate")
    protected fun typeText(keys: List<KeyStroke>): Editor {
        val editor = myFixture.editor
        for (key in keys) {
            facade.dispatchTypedKey(KeyEvent(editor.component,
                0, 0, key.modifiers, key.keyCode, key.keyChar
            ))
        }
        return editor
    }

    fun assertOffset(vararg expectedOffsets: Int) {
        val carets = myFixture.editor.caretModel.allCarets
        assertEquals("Wrong amount of carets", expectedOffsets.size, carets.size)
        for (i in expectedOffsets.indices) {
            assertEquals(expectedOffsets[i], carets[i].offset)
        }
    }

    fun assertSelection(expected: String?) {
        val selected = myFixture.editor.selectionModel.selectedText
        assertEquals(expected, selected)
    }

    fun doTest(keys: List<KeyStroke>, before: String, after: String) {
        doTest(before, after) {
            typeText(keys)
        }
    }

    fun doTest(before: String, after: String, block: () -> Unit) {
        configureByText(before)
        CommandProcessor.getInstance().executeCommand(myFixture.project, {
            ApplicationManager.getApplication().runWriteAction {
                block()
            }
        }, "testCommand", "org.neojet")
        myFixture.checkResult(after)
    }

    fun keys(asTyped: String) = asTyped.map {
        KeyStroke.getKeyStroke(it)
    }
}