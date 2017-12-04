package org.neojet.events

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.type.TypeFactory
import io.neovim.java.event.EventsManager
import io.neovim.java.rpc.NeovimObjectMapper
import io.neovim.java.rpc.Packet
import org.junit.Before
import org.junit.Test
import org.msgpack.core.MessagePack


/**
 * @author dhleong
 */
class BufferEventTest {

    private val typeFactory = TypeFactory.defaultInstance()

    private lateinit var manager: EventsManager

    @Before fun setUp() {
        manager = EventsManager().apply {
            register(TextChangedEvent::class.java)
        }
    }

    @Test fun `Get event type for BufferEvent args`() {
        assert(manager.getEventValueType("text_changed"))
            .isEqualTo(genericType(
                BufferEvent.BufferEventArg::class.java,
                TextChangedEvent.Change::class.java
            ))
    }

    @Test fun `Read custom Event`() {
        val pack = MessagePack.newDefaultBufferPacker()
        pack.packArrayHeader(3)
        pack.packInt(Packet.Type.NOTIFICATION.ordinal)
        pack.packString("text_changed")
        pack.packArrayHeader(3)
        pack.packInt(42) // buffer number
        pack.packInt(9001) // cursor offset
        pack.packMapHeader(4)
        pack.packString("type")
        pack.packString("incremental")
        pack.packString("start")
        pack.packInt(42)
        pack.packString("end")
        pack.packInt(42)
        pack.packString("text")
        pack.packString("Can't take the skies from me")

        val eventsManager = EventsManager()
        eventsManager.register(TextChangedEvent::class.java)
        val mapper = NeovimObjectMapper.newInstance(eventsManager, true)
        val packet = mapper.readValue(
            pack.toByteArray(),
            Packet::class.java
        )

        assert(packet).isInstanceOf(TextChangedEvent::class)
        val event = packet as TextChangedEvent
        assertAll {
            assert(event.value().bufferId).isEqualTo(42)
            assert(event.value().cursorOffset).isEqualTo(9001)
        }

        val arg = event.arg
        assert(arg).isInstanceOf(TextChangedEvent.Change::class)
    }

    private fun genericType(rawType: Class<*>, paramType: Class<*> = Any::class.java): JavaType =
        typeFactory.constructParametricType(rawType, paramType)
}