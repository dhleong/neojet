package io.neovim.java;

import com.fasterxml.jackson.databind.JsonNode;
import io.neovim.java.rpc.ResponsePacket;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.neovim.java.NeovimAssertions.assertThat;

/**
 * Integration tests
 *
 * @author dhleong
 */
public class NeovimTest extends EmbeddedNeovimTest {

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

    @Test
    public void uiAttach() {
        assertThat(
            nvim.uiAttach(90, 24, true)
                .blockingGet()
        ).isTrue();
        nvimCommand("e ~/.bash_profile");

        assertThat(
            nvim.commandOutput("echo \"test\"")
                .blockingGet()
        ).isEqualTo("\ntest");

        JsonNode node = nvim.<JsonNode>notifications("redraw")
            .timeout(5, TimeUnit.SECONDS)
            .firstOrError()
            .blockingGet();
        assertThat(node)
            .isNotNull();
    }
}
