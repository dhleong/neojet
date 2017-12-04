package org.neojet.integrate

import com.intellij.util.io.inputStream
import com.intellij.util.io.isDirectory
import com.intellij.util.io.lastModified
import io.neovim.java.Neovim
import io.reactivex.Single
import org.neojet.NJCore
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author dhleong
 */
class VimIntegrator(
    private val homeDirectory: File
) {
    companion object {
        private const val vimResourcesRoot = "vim/"

        val instance by lazy {
            val home = File(System.getProperty("user.home"))
            VimIntegrator(home)
        }
    }

    fun getLocalFile(resourcePath: String): File {
        ensureLocalFiles()

        val outputPath = resourcePath.substring(vimResourcesRoot.length)

        val file = File(getLocalFilesRoot(), outputPath)

        val container = file.parentFile
        if (!(container.isDirectory || container.mkdirs())) {
            throw IllegalStateException(
                "Couldn't create local file for $outputPath in $file")
        }

        if (!file.exists()) {
            throw IllegalStateException(
                "Local file for $resourcePath ($file) not created!"
            )
        }

        return file
    }

    private fun getLocalFilesRoot(): File {
        val root = File(homeDirectory, ".config/neojet")
        return File(root, "integration").also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
    }

    private fun ensureLocalFiles(root: String = vimResourcesRoot) {
        val classLoader = NJCore::class.java.classLoader
        val rootUri = classLoader.getResource(root).toURI()
        var fileSystem: FileSystem? = null
        val rootPath = if (rootUri.scheme == "jar") {
            val fs = FileSystems.newFileSystem(rootUri, mapOf<String, Any>(), classLoader).also {
                fileSystem = it
            }
            fs.getPath(root)
        } else {
            // TODO actually if we're a local file, we probably don't
            //  need to bother copying ourselves elsewhere....
            Paths.get(rootUri)
        }

        val localRoot = getLocalFilesRoot()

        try {
            Files.walk(rootPath)
                .filter { it !== rootPath }
                .filter { !it.fileName.toString().startsWith(".") }
                .forEach {
                    // NOTE: if the path is not absolute, we're in the jar;
                    //  due to how ZipPath handles relativize(), we just manually
                    //  drop the first "name" (which is just /vim) to get a
                    //  matching relative path
                    val name =
                        if (rootPath.isAbsolute) rootPath.relativize(it)
                        else it.subpath(1, it.nameCount)

                    val outputFile = File(localRoot, name.toString())
                    ensureUpToDate(it, outputFile)
                }
        } finally {
            fileSystem?.close()
        }
    }

    private fun ensureUpToDate(inputPath: Path, outputFile: File) {
        if (inputPath.isDirectory()) {
            // it's supposed to be a directory. make it so.
            if (!outputFile.isDirectory) outputFile.mkdirs()
            return
        }

        if (!isOutOfDate(inputPath, outputFile)) {
            // up to date. We're good to go!
            return
        }

        // out of date! update the file
        System.out.println("$outputFile is out of date!")
        inputPath.inputStream().buffered().use { input ->
            outputFile.outputStream().buffered().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun isOutOfDate(inputPath: Path, file: File): Boolean {
        val inputMod = inputPath.lastModified()
        val outputMod = file.lastModified()
        return inputMod.toMillis() > outputMod
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