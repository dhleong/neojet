package io.neovim.java.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.neovim.java.NeovimAssertions.assertThat
import io.neovim.java.event.redraw.PutEvent
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
            ["redraw",
                [["put", ["a"], ["b"]]]
            """,
            RedrawEvent::class.java
        )

        assertThat(event.subEvents).hasSize(1)

        assertThat(event.subEvents[0])
            .isInstanceOf(PutEvent::class.java)

        val put = event.subEvents[0] as PutEvent
        assertThat(put.value).hasSize(2)

//        assertThat(put.value[0].value).isEqualTo("a")
//        assertThat(put.value[1].value).isEqualTo("b")
    }
}