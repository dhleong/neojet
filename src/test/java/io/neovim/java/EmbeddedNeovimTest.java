package io.neovim.java;

import org.junit.After;
import org.junit.Before;

import static io.neovim.java.NeovimAssertions.assertThat;

/**
 * Base-class for integration tests that use a Neovim
 *  connected to an embedded instance
 *
 * @author dhleong
 */
public abstract class EmbeddedNeovimTest {

    protected Neovim nvim;
    protected Rpc rpc;

    @Before
    public void setUp() throws Exception {
        rpc = Rpc.createEmbedded(true);
        nvim = Neovim.attach(rpc);
    }

    @After
    public void tearDown() {
        nvim.close();
    }

    protected void nvimCommand(String command) {
        assertThat(
            nvim.command(command).blockingGet()
        ).isTrue();
    }

    protected void nvimInput(String keys) {
        assertThat(
            nvim.input(keys).blockingGet()
        ).isEqualTo(keys.length());
    }
}
