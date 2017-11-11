package org.neojet

import io.neovim.java.event.redraw.ScrollEvent
import io.neovim.java.event.redraw.SetScrollRegionEvent

/**
 * @author dhleong
 */
class ScrollTest : NeojetTestCase() {
    fun `test Handle Scroll Up Event at end of file`() = doTest(
        before = """
            class Test {
                void foo() {
                }

            }
        """.trimIndent(),

        after = """
            class Test {
                void foo() {
                }
            }

        """.trimIndent()) {
        // TODO the extra line after is probably incorrect...

        // test
        facade.setScrollRegion(SetScrollRegionEvent(
            0, 3, 59, 22
        ))

        facade.scroll(ScrollEvent(1))
    }

    fun `test Handle Scroll Down Event at top of file`() = doTest(
        before = """
            class Test {
                void foo() {
                }

            }
        """.trimIndent(),

        after = """
            class Test {

                void foo() {
                }

            }
        """.trimIndent()) {

        // test
        facade.setScrollRegion(SetScrollRegionEvent(
            0, 1, 59, 22
        ))

        facade.scroll(ScrollEvent(-1))
    }
}