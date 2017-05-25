package org.neojet

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import io.neovim.java.IntPair
import io.neovim.java.Neovim
import org.neojet.util.component1
import org.neojet.util.component2
import org.neojet.util.disposable
import org.neojet.util.getEditorFont
import org.neojet.util.vFile
import java.awt.KeyboardFocusManager
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JComponent
import kotlin.reflect.KProperty

class NJCore : ApplicationComponent, Disposable {

    companion object {
        val COMPONENT_NAME = "NJCore"

        val instance: NJCore by object {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): NJCore {
                return ApplicationManager.getApplication().getComponent(COMPONENT_NAME)
                    as NJCore
            }
        }
    }
    val logger = Logger.getLogger("NeoJet:NJCore")!!

    var nvim: Neovim? = null
    var refs = AtomicInteger(0)

    override fun getComponentName(): String = COMPONENT_NAME

    override fun disposeComponent() {
        nvim?.close()
        nvim = null

//        TypedActionFacade.Instance.restoreHandler()
    }

    override fun dispose() {
        Disposer.dispose(this)
    }

    override fun initComponent() {
//        Traceur.enableLogging()

        try {
            nvim = Neovim.attachEmbedded()
        } catch (e: Throwable) {
            logger.log(Level.WARNING,
                "Unable to initialize Neovim connection. Is it installed?",
                e)
            return
        }

        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorReleased(event: EditorFactoryEvent) {
                event.editor.getUserData(NEOJET_ENHANCED_EDITOR)?.let {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .removeKeyEventDispatcher(it.keyEventDispatcher)
                }
            }

            override fun editorCreated(event: EditorFactoryEvent) {
                // TODO we can probably get away with a single KeyEventDispatcher
                val facade = NeojetEnhancedEditorFacade.install(event.editor)
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .addKeyEventDispatcher(facade.keyEventDispatcher)
            }
        }, this)
    }

    fun attach(editor: NeojetTextFileEditor): Neovim {
        nvim?.let { nvim ->
            uiAttach(nvim, editor.getEditor(), editor.vFile, IntPair(
                editor.panel.cols,
                editor.panel.rows
            ))

            editor.panel.isAttachedToUi = true

            return nvim
        }

        throw IllegalStateException("No nvim")
    }

    fun attach(editor: Editor): Neovim {
        nvim?.let { nvim ->
            val (textWidth, textHeight) = editor.component.textDimensions
            uiAttach(nvim, editor, editor.document.vFile, IntPair(
                maxOf(60, editor.component.width / textWidth),
                maxOf(25, editor.component.height / textHeight)
            ))

            return nvim
        }

        throw IllegalStateException("No nvim")
    }

    private fun uiAttach(nvim: Neovim, editor: Editor, vFile: VirtualFile, windowSize: IntPair) {
        Disposer.register(editor.disposable, Disposable {
            if (0 == refs.decrementAndGet()) {
                logger.info("detach last")
                nvim.uiDetach()
                    .blockingGet()
            }
        })

        if (0 == refs.getAndIncrement()) {
            val (width, height) = windowSize
            logger.info("attach: $width, $height")
            nvim.uiAttach(width, height, true)
                .blockingGet()
        }

        val filePath = vFile.path
        nvim.command("e! $filePath").blockingGet()
        editor.putUserData(NVIM_BUFFER_KEY, nvim.current.buffer().blockingGet())
    }

}

private val JComponent.textDimensions: IntPair
    get() {
        val font = getEditorFont()
        val fontMetrics = getFontMetrics(font)
        return IntPair(
            fontMetrics.charWidth('M'),
            fontMetrics.height
        )
    }

