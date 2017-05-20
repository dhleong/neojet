package io.neovim.java;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        // FIXME IntPairAssert object
        IntPair oldCursor = window.cursor().blockingGet();
        assertThat(oldCursor.row()).isEqualTo(1);
        assertThat(oldCursor.col()).isEqualTo(0);

        assertThat(
            nvim.command("exe 'go ' . (line2byte(2) + 3)").blockingGet()
        ).isTrue();

        IntPair newCursor = window.cursor().blockingGet();
        assertThat(newCursor.row()).isEqualTo(2);
        assertThat(newCursor.col()).isEqualTo(3);
    }

    @Test
    public void setCursor() {
        window.setCursor(3, 4).blockingGet();

        IntPair newCursor = window.cursor().blockingGet();
        assertThat(newCursor.row()).isEqualTo(3);
        assertThat(newCursor.col()).isEqualTo(4);
    }
}