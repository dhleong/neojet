package io.neovim.java;

import org.junit.Before;
import org.junit.Test;

import static io.neovim.java.NeovimAssertions.assertThat;

/**
 * @author dhleong
 */
public class WindowTest extends EmbeddedNeovimTest {
    private Window window;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        nvimCommand("e serenity.ship");
        window = nvim.current.window().blockingGet();
        Buffer buffer = window.buffer().blockingGet();

        // fill with some text
        buffer.lines().replace(
            "Take my love",
            "Take my land",
            "Take me where I cannot stand"
        ).blockingGet();

        assertThat(buffer.lines().get().toList().blockingGet())
            .containsExactly(
                "Take my love",
                "Take my land",
                "Take me where I cannot stand"
            );
    }

    @Test
    public void getCursor() {
        IntPair oldCursor = window.cursor().blockingGet();
        assertThat(oldCursor)
            .hasFirst(1)
            .hasSecond(0);

        assertThat(
            nvim.command("exe 'go ' . (line2byte(2) + 3)").blockingGet()
        ).isTrue();

        IntPair newCursor = window.cursor().blockingGet();
        assertThat(newCursor)
            .hasFirst(2)
            .hasSecond(3);
    }

    @Test
    public void setCursor() {
        window.setCursor(3, 4).blockingGet();

        IntPair newCursor = window.cursor().blockingGet();
        assertThat(newCursor)
            .hasFirst(3)
            .hasSecond(4);
    }
}