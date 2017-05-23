package io.neovim.java.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.neovim.java.NeovimAssertions.assertThat
import io.neovim.java.event.redraw.CursorGotoEvent
import io.neovim.java.event.redraw.PutEvent
import io.neovim.java.event.redraw.UnknownRedrawEvent
import io.neovim.java.event.redraw.UpdateColorEvent
import io.neovim.java.rpc.NeovimObjectMapper
import org.junit.Before
import org.junit.Test

/**
 * @author dhleong
 */
class RedrawEventTest {

    lateinit var mapper: ObjectMapper

    @Before fun setUp() {
        mapper = NeovimObjectMapper.newInstance()
    }

    @Test fun readPut() {
        val event = mapper.readValue(
            """
            |[2,
            | "redraw",
            | [["put", ["a"], ["b"]]]
            |]
            """.trimMargin(),
            RedrawEvent::class.java
        )

        assertThat(event.args).hasSize(1)

        assertThat(event.args[0])
            .isInstanceOf(PutEvent::class.java)

        val put = event.args[0] as PutEvent
        assertThat(put.value).hasSize(2)

        assertThat(put.value[0].value).isEqualTo('a')
        assertThat(put.value[1].value).isEqualTo('b')
    }

    @Test fun readCursorGoto() {
        val event = mapper.readValue(
            """
            |[2,
            | "redraw",
            | [["cursor_goto", [42, 9001]]]
            |]
            """.trimMargin(),
            RedrawEvent::class.java
        )

        assertThat(event.args).hasSize(1)

        assertThat(event.args[0])
            .isInstanceOf(CursorGotoEvent::class.java)

        val put = event.args[0] as CursorGotoEvent
        assertThat(put.value).hasSize(1)

        assertThat(put.value[0])
            .hasFirst(42)
            .hasSecond(9001)
    }

    @Test fun readUpdateColor() {
        val event = mapper.readValue(
            """
            |[2,
            | "redraw",
            | [["update_fg", [14474444]]]
            |]
            """.trimMargin(),
            RedrawEvent::class.java
        )

        assertThat(event.args).hasSize(1)

        assertThat(event.args[0])
            .isInstanceOf(UpdateColorEvent::class.java)

        val put = event.args[0] as UpdateColorEvent
        assertThat(put.value).hasSize(1)

    }

    @Test fun readUnknown() {
         val event = mapper.readValue(
            """
            |[2,
            | "redraw",
            | [["crazy_ivan", ["here's something"], ["you", "can't", "do"]]]
            |]
            """.trimMargin(),
            RedrawEvent::class.java
        )

        assertThat(event.args).hasSize(1)

        assertThat(event.args[0])
            .isInstanceOf(UnknownRedrawEvent::class.java)

        val put = event.args[0] as UnknownRedrawEvent
        assertThat(put.value).hasSize(2)
    }
}