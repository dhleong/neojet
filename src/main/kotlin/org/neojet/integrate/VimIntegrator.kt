package org.neojet.integrate

import io.neovim.java.Neovim
import io.reactivex.Single
import org.neojet.NJCore
import java.io.File

/**
 * @author dhleong
 */
class VimIntegrator {
    companion object {
        val instance by lazy {
            VimIntegrator()
        }
    }

    fun getLocalFile(resourcePath: String): File {
        val file = File(getLocalFilesRoot(), resourcePath)

        val container = file.parentFile
        if (!(container.isDirectory || container.mkdirs())) {
            throw IllegalStateException(
                "Couldn't create local file for $resourcePath in $file")
        }

        if (!file.exists() || isOutOfDate(file)) {
            val classLoader = NJCore::class.java.classLoader
            classLoader.getResourceAsStream(resourcePath).buffered().use { input ->
                file.outputStream().buffered().use { out ->
                    input.copyTo(out)
                }
            }
        }

        return file
    }

    // TODO proper check to avoid unnecessary writes to disk
    @Suppress("UNUSED_PARAMETER")
    private fun isOutOfDate(file: File): Boolean = true

    private fun getLocalFilesRoot(): File {
        val home = File(System.getProperty("user.home"))
        val root = File(home, ".config/neojet")
        return File(root, "integration").also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
    }
}

/**
 * Util to ask the nvim instance to :source a bundled resource file
 */
fun Neovim.sourceRes(path: String): Single<Boolean> {
    try {
        val file = VimIntegrator.instance.getLocalFile(path)
        val filePath = file.absolutePath.replace(" ", "\\ ")
        return this.command("source $filePath")
    } catch (e: Throwable) {
        e.printStackTrace()
        throw e;
    }
}