package io.neovim.java;

import io.neovim.java.rpc.ResponsePacket;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.neovim.java.NeovimAssertions.assertThat;

/**
 * Integration tests
 *
 * @author dhleong
 */
public class NeovimTest {

    Neovim nvim;

    @Before
    public void setUp() {
        nvim = Neovim.attachEmbedded();
    }

    public void tearDown() {
        nvim.close();
    }

    @Test
    public void getApiInfo() {
        ResponsePacket packet =
            nvim.getApiInfo()
                .blockingGet();
        assertThat(packet)
            .isNotNull()
            .hasError(null);
        assertThat(packet.result).isInstanceOf(List.class);
    }

}
