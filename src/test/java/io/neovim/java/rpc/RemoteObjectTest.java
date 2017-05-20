package io.neovim.java.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neovim.java.Buffer;
import io.neovim.java.EmbeddedNeovimTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class RemoteObjectTest extends EmbeddedNeovimTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        nvim.getApiInfo().blockingGet();
        nvimCommand("e serenity.ship");
    }

    @Test
    public void symmetrical_Buffer() throws IOException {
        Buffer b = nvim.current.buffer().blockingGet();

        ObjectMapper mapper =
            NeovimObjectMapper.newInstance(rpc, Collections.emptyMap());

        byte[] bytes = mapper.writeValueAsBytes(b);

        Object inflated = mapper.readValue(bytes, Object.class);
        assertThat(inflated)
            .isEqualTo(b);
    }
}
