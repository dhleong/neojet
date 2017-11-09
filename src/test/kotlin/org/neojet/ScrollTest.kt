package org.neojet

import io.neovim.java.event.redraw.ScrollEvent
import io.neovim.java.event.redraw.SetScrollRegionEvent

/**
 * @author dhleong
 */
class ScrollTest : NeojetTestCase() {
    fun `test Handle Scroll Event at end of file`() = doTest(
        before = """
            class Test {
                void foo() {
                }

            }
        """.trimMargin(),

        after = """
            class Test {
                void foo() {
                }
            }
        """.trimMargin()) {

        // test
        facade.setScrollRegion(SetScrollRegionEvent(
            0, 3, 22, 4
        ))

        facade.scroll(ScrollEvent(1))
    }
}