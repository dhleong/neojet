package io.neovim.java.rpc;

import io.neovim.java.Rpc;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dhleong
 */
public class EmbedChannel implements Rpc.Channel {

    final ArrayList<String> invocation;
    Process process;

    public EmbedChannel() {
        this(Collections.emptyList());
    }
    public EmbedChannel(List<String> args) {
        this("nvim", args);
    }
    public EmbedChannel(String exe, List<String> args) {
        invocation = new ArrayList<>(args.size() + 2);
        invocation.add(exe);
        invocation.addAll(args);
        invocation.add("--embed");
    }

    @Override
    public InputStream getInputStream() {
        return startIfNeeded().getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return startIfNeeded().getErrorStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return startIfNeeded().getOutputStream();
    }

    @Nonnull Process startIfNeeded() {
        final Process existing = process;
        if (existing != null) return existing;
        try {
            return process = new ProcessBuilder(invocation).start();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to start nvim (" + invocation + "); " +
                    "make sure it's installed!",
                    e
            );
        }
    }
}
