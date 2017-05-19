package io.neovim.java;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.neovim.java.NeovimAssertions.assertThat;

/**
 * @author dhleong
 */
public class BufferTest extends EmbeddedNeovimTest {

    Buffer buffer;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        nvim.command("e serenity.ship");
        buffer = nvim.current.buffer().blockingGet();
    }

    @Test
    public void append() {
        buffer.append(Arrays.asList("Take my love", "Take my land"))
            .blockingGet();

        List<String> lines = buffer.lines().get().toList().blockingGet();
        assertThat(lines)
            .containsExactly("", "Take my love", "Take my land");
    }

    @Test
    public void insert() {
        buffer.insert(Arrays.asList("Take my love", "Take my land"), 0)
            .blockingGet();

        List<String> lines = buffer.lines().get().toList().blockingGet();
        assertThat(lines)
            .containsExactly("Take my love", "Take my land", "");
    }

    @Test
    public void deleteLines() {
        buffer.insert(Arrays.asList("Take my love", "Take my land"), 0)
            .blockingGet();

        buffer.lines().delete().blockingGet();

        assertThat(buffer.lines().get().toList().blockingGet())
            .containsExactly("");
    }
}