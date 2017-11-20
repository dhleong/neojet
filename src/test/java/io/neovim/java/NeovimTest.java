package io.neovim.java;

import io.neovim.java.event.RedrawEvent;
import io.neovim.java.event.redraw.CursorGotoEvent;
import io.neovim.java.event.redraw.PutEvent;
import io.neovim.java.event.redraw.RedrawSubEvent;
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
            nvim.uiAttach(90, 24)
                .blockingGet()
        ).isTrue();
        nvimCommand("e ~/.bash_profile");

        assertThat(
            nvim.commandOutput("echo \"test\"")
                .blockingGet()
        ).isEqualTo("\ntest");

        List<RedrawSubEvent<?>> subEvents = nvim.notifications(RedrawEvent.class)
            .timeout(5, TimeUnit.SECONDS)
            .firstOrError()
            .blockingGet();
        assertThat(subEvents)
            .isNotNull()
            .size().isGreaterThan(1);

        assertThat(subEvents)
            .hasAtLeastOneElementOfType(PutEvent.class)
            .hasAtLeastOneElementOfType(CursorGotoEvent.class);
    }

}
