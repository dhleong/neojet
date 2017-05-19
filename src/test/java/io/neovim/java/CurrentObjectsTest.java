package io.neovim.java;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author dhleong
 */
public class CurrentObjectsTest {
    Neovim nvim;

    @Before
    public void setUp() throws Exception {
        nvim = Neovim.attachEmbedded();
        nvim.command("e serenity.ship");
    }

    @After
    public void tearDown() throws Exception {
        nvim.close();
    }

    @Test
    public void getBuffer() {
        Buffer b = nvim.current.buffer().blockingGet();
        assertThat(b).isNotNull();

        assertThat(b.name().blockingGet())
            .endsWith("/serenity.ship");
    }
}