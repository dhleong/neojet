package io.neovim.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neovim.java.rpc.NeovimObjectMapper;
import org.junit.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class IntPairTest {

    @Test
    public void serializeTest() throws IOException {
        ObjectMapper mapper = NeovimObjectMapper.newInstance();
        byte[] bytes = mapper.writeValueAsBytes(new IntPair(42, 9001));

        MessageBufferPacker pack = MessagePack.newDefaultBufferPacker();
        pack.packArrayHeader(2);
        pack.packInt(42);
        pack.packInt(9001);

        assertThat(bytes)
            .isEqualTo(pack.toByteArray());
    }
}