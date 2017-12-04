package org.neojet.integrate

import assertk.assert
import assertk.assertions.exists
import assertk.assertions.isDirectory
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * @author dhleong
 */
class VimIntegratorTest {

    private lateinit var integrator: VimIntegrator
    private lateinit var localTestDir: File

    @Before fun setUp() {
        localTestDir = Files.createTempDirectory("neojet-test").toFile()
        integrator = VimIntegrator(localTestDir)
    }

    @After fun tearDown() {
        localTestDir.deleteRecursively()
    }

    @Test fun `Distribute all vim files to local directory`() {
        val file = integrator.getLocalFile("vim/plugin/neojet.vim")
        assert(file).exists()
        assert(File(file.parentFile.parentFile, "autoload/neojet")).isDirectory()
    }

}