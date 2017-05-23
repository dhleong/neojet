package org.neojet

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.util.Disposer
import io.neovim.java.Neovim
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KProperty

class NJCore : ApplicationComponent {
    companion object {
        val COMPONENT_NAME = "NJCore"

        val instance: NJCore by object {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): NJCore {
                return ApplicationManager.getApplication().getComponent(COMPONENT_NAME)
                    as NJCore
            }
        }
    }
    val logger = Logger.getLogger("NeoJet:NJCore")

    var nvim: Neovim? = null
    var refs = AtomicInteger(0)

    override fun getComponentName(): String = COMPONENT_NAME

    override fun disposeComponent() {
        nvim?.close()
        nvim = null

//        TypedActionFacade.Instance.restoreHandler()
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

//        TypedActionFacade.Instance.installHandler({ original ->
//            NeovimTypedActionHandler(original)
//        })

//        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
//            override fun editorReleased(event: EditorFactoryEvent) {
//                TODO("not implemented")
//            }
//
//            override fun editorCreated(event: EditorFactoryEvent) {
//                event.editor.component.addKeyListener(object : KeyAdapter() {
//                    override fun keyTyped(e: KeyEvent?) {
//                        e?.let {
//                            System.out.println(it)
//                        }
//                    }
//                })
//            }
//        })
    }

    fun attach(editor: NeojetTextFileEditor): Neovim {
        if (nvim == null) throw IllegalStateException("No nvim")

        nvim!!.let { nvim ->
            Disposer.register(editor, Disposable {
                if (0 == refs.decrementAndGet()) {
                    nvim.uiDetach()
                    logger.info("detach last")
                }
            })

            if (0 == refs.getAndIncrement()) {
                val width = editor.panel.cols
                val height = editor.panel.rows
                logger.info("attach: $width, $height")
                nvim.uiAttach(width, height, true)
                    .blockingGet()
            }

            val filePath = editor.vFile.path
            nvim.command("e! $filePath").blockingGet()
            editor.putUserData(NVIM_BUFFER_KEY, nvim.current.buffer().blockingGet())
            editor.panel.isAttachedToUi = true

            return nvim
        }
    }

}