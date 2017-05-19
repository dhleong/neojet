package io.neovim.java.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neovim.java.Buffer;
import io.neovim.java.Neovim;
import io.neovim.java.Rpc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class RemoteObjectTest {
    Neovim nvim;
    Rpc rpc;

    @Before
    public void setUp() throws Exception {
        rpc = Rpc.createEmbedded();
        nvim = Neovim.attach(rpc);
        nvim.getApiInfo().blockingGet();
        nvim.command("e serenity.ship");
    }

    @After
    public void tearDown() throws Exception {
        nvim.close();
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
