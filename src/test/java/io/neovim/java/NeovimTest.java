package io.neovim.java;

import io.neovim.java.rpc.NotificationPacket;
import io.neovim.java.rpc.ResponsePacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @After
    public void tearDown() throws InterruptedException {
        nvim.close();
    }

    @Test
    public void empty() {
        // just a simple setup and tearDown test
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

    @Test
    public void uiAttach() {
        System.out.println("<< uiAttach");
        nvim.uiAttach(90, 24, true);
        System.out.println("   uiAttach >>");
        nvim.command("e ~/.bash_profile");
        System.out.println("   output >>");
        nvim.commandOutput("echo \"test\"").blockingGet();
        System.out.println("   command >>");
        NotificationPacket packet = nvim.notifications("redraw")
            .timeout(5, TimeUnit.SECONDS)
            .firstOrError()
            .blockingGet();
        assertThat(packet)
            .isNotNull()
            .hasEvent("redraw");
    }
}
