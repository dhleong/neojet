package io.neovim.java;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author dhleong
 */
public class CurrentObjectsTest extends EmbeddedNeovimTest {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        nvimCommand("e serenity.ship");
    }

    @Test
    public void getBuffer() {
        Buffer b = nvim.current.buffer().blockingGet();
        assertThat(b).isNotNull();

        assertThat(b.name().blockingGet())
            .endsWith("/serenity.ship");
    }

    @Test
    public void getWindow() {
        Window w = nvim.current.window().blockingGet();
        assertThat(w).isNotNull();

        Buffer actualBuf = nvim.current.buffer().blockingGet();
        Buffer windowBuf = w.buffer().blockingGet();
        assertThat(windowBuf)
            .isEqualTo(actualBuf);
    }

    @Test
    public void getTabpage() {
        Tabpage t = nvim.current.tabpage().blockingGet();
        assertThat(t).isNotNull();

        Window actualWin = nvim.current.window().blockingGet();
        Window tabpageWin = t.window().blockingGet();
        assertThat(tabpageWin)
            .isEqualTo(actualWin);
    }
}