package io.neovim.java.rpc.channel;

import io.neovim.java.Rpc;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dhleong
 */
public class EmbedChannel implements Rpc.Channel {

    final ArrayList<String> invocation;
    Process process;

    /**
     * Create an Embed-based Channel, optionally
     * passing extra args to the nvim process
     * @param args
     */
    public EmbedChannel(String... args) {
        this(Arrays.asList(args), true);
    }

    /**
     * Create an Embed-based Channel, specifying
     * the full invocation to use when starting nvim
     * @param invocation MUST include the path to nvim
     */
    public EmbedChannel(List<String> invocation) {
        this(invocation, false);
    }

    private EmbedChannel(List<String> invocation, boolean needExe) {
        int extra = needExe ? 1 : 3;
        this.invocation = new ArrayList<>(invocation.size() + extra);
        if (needExe) {
            this.invocation.add("/usr/bin/env");
            this.invocation.add("nvim");
        }
        this.invocation.addAll(invocation);

        if (!this.invocation.contains("--embed")) {
            this.invocation.add("--embed");
        }
    }

    @Override
    public void close() {
        Process process = this.process;
        if (process != null) {
            try {
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            } catch (IOException e) {
                // ignore
            }

            process.destroy();
        }
        this.process = null;
    }

    @Override
    public void tryOpen() throws Exception {
        startIfNeeded();
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

    @Override
    public String toString() {
        return "EmbedChannel{" +
            "invocation=" + invocation +
            '}';
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
