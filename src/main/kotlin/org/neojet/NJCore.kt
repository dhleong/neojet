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
import io.neovim.java.Rpc
import io.neovim.java.rpc.NotificationPacket
import io.reactivex.disposables.CompositeDisposable
import org.neojet.events.registerCustomEvents
import org.neojet.gui.UiThreadScheduler
import org.neojet.integrate.sourceRes
import org.neojet.util.absoluteLocalFile
import org.neojet.util.component1
import org.neojet.util.component2
import org.neojet.util.disposable
import org.neojet.util.vFile
import java.awt.KeyboardFocusManager
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KProperty

const val isUsingTextFileEditor: Boolean = true

class NJCore : ApplicationComponent, Disposable {

    companion object {
        val COMPONENT_NAME = "NJCore"

        val instance: NJCore by object {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): NJCore {
                return ApplicationManager.getApplication().getComponent(COMPONENT_NAME)
                    as NJCore
            }
        }

        var isTestMode: Boolean = false
    }
    val logger = Logger.getLogger("NeoJet:NJCore")!!

    val subs = CompositeDisposable()
    var nvim: Neovim? = null
    var refs = AtomicInteger(0)

    override fun getComponentName(): String = COMPONENT_NAME

    override fun disposeComponent() {
        subs.clear()
        nvim?.close()
        nvim = null
    }

    override fun dispose() {
        Disposer.dispose(this)
    }

    override fun initComponent() {
//        Traceur.enableLogging()

        if (isTestMode) return

        val nvim = try {
            Neovim.attachEmbedded(true)
                .registerCustomEvents()

            // NOTE: this is for testing. Run nvim like this:
            //  $ NVIM_LISTEN_ADDRESS=127.0.0.1:6666 nvim
//            nvim = Neovim.attachSocket("127.0.0.1", 7777).registerCustomEvents()
        } catch (e: Throwable) {
            logger.log(Level.WARNING,
                "Unable to initialize Neovim connection. Is it installed?",
                e)
            return
        }

        this.nvim = nvim

        nvim.sourceRes("vim/init.vim")
            .blockingGet()

        subs.add(
            nvim.notifications()
                .filter { it.event != "redraw" }
                .observeOn(UiThreadScheduler.instance)
                .subscribe(this::onNotification)
        )

        @Suppress("ConstantConditionIf")
        if (!isUsingTextFileEditor) {
            EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
                override fun editorReleased(event: EditorFactoryEvent) {
                    event.editor.getUserData(neojetEnhancedEditor)?.let {
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
    }

    fun attach(editor: NeojetTextFileEditor): Neovim {
        nvim?.let { nvim ->
            uiAttach(nvim, editor.editor, editor.vFile, IntPair(
                editor.panel.cols,
                editor.panel.rows
            ))

            editor.panel.isAttachedToUi = true

            return nvim
        }

        throw IllegalStateException("No nvim")
    }

    fun attach(editor: Editor, enhanced: NeojetEnhancedEditorFacade): Neovim {
        nvim?.let { nvim ->
            uiAttach(nvim, editor, editor.document.vFile, enhanced.cells)

            // attaching to enhanced editor? let's enforce some configs:
            nvim.command("setlocal nolist") // or we could parse it and replace them...
                .blockingGet()

            return nvim
        }

        if (!isTestMode) {
            throw IllegalStateException("No nvim")
        }

        // test mode gets this bogus Rpc object
        return Neovim.attach(Rpc.create(object : Rpc.Channel {
            override fun tryOpen() { }

            override fun getInputStream(): InputStream =
                object : InputStream() {
                    override fun read() = -1
                }

            override fun getErrorStream(): InputStream =
                object : InputStream() {
                    override fun read() = -1
                }

            override fun getOutputStream(): OutputStream =
                object : OutputStream() {
                    override fun write(b: Int) { }
                }

            override fun close() { }
        }, true))
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

            // TODO we probably need to force-disable syntax highlighting
            // NOTE: This was a good idea, but nvim doesn't seem to respect
            // more than 999 rows. It would be nice to assume files we're editing
            // won't go over that, but I learned recently that one of Telegram's
            // Activity class files is over 10K lines long, so....
//            // Use a really high viewport so we know any Scroll commands
//            //  are actually for adjusting text positions
//            nvim.uiAttach(width, 20000, true)
            nvim.uiAttach(width, height)
                .blockingGet()
        }

        val filePath = vFile.absoluteLocalFile.absolutePath
        nvim.command("e! $filePath").blockingGet()
        val buf = nvim.current.buffer().blockingGet()
        editor.putUserData(NVIM_BUFFER_KEY, buf)
    }

    internal fun onNotification(packet: NotificationPacket<Any>) {
        System.out.println("TODO handle $packet")
    }

}


