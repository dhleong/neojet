package io.neovim.java;

import io.neovim.java.rpc.NotificationPacket;
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
        nvim.uiAttach(90, 24, true);
        nvim.command("e ~/.bash_profile");

        assertThat(
            nvim.commandOutput("echo \"test\"")
                .blockingGet()
        ).isEqualTo("\ntest");

        NotificationPacket packet = nvim.notifications("redraw")
            .timeout(5, TimeUnit.SECONDS)
            .firstOrError()
            .blockingGet();
        assertThat(packet)
            .isNotNull()
            .hasEvent("redraw");
    }
}
