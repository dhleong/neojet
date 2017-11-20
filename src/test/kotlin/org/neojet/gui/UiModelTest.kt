package org.neojet.gui

import assertk.assert
import assertk.assertions.isEqualTo
import io.neovim.java.event.redraw.ScrollEvent
import io.neovim.java.event.redraw.SetScrollRegionEvent
import org.junit.Before
import org.junit.Test

/**
 * @author dhleong
 */
class UiModelTest {

    lateinit var model: UiModel

    @Before fun setUp() {
        val initialContents = """
            |package net.serenity.firefly;
            |
            |public class Firefly {
            |
            |
            |}""".trimMargin()

        model = UiModel()
        model.resize(6, 42)
        model.setContents(initialContents)
        model.setScrollRegion(SetScrollRegionEvent(0, 0, 41, 5))

        assert(model.getContents()).isEqualTo(initialContents)
    }

    @Test fun `Scroll the whole window up`() {
        model.scroll(ScrollEvent(2))

        assert(model.getContents())
            .isEqualTo("""
                |public class Firefly {
                |
                |
                |}
                |
                |""".trimMargin())
    }

    @Test fun `Scroll the whole window down`() {
        model.scroll(ScrollEvent(-2))

        assert(model.getContents())
            .isEqualTo("""
                |
                |
                |package net.serenity.firefly;
                |
                |public class Firefly {
                |""".trimMargin())
    }
}