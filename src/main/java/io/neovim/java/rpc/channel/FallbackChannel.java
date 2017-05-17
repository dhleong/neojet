package io.neovim.java.rpc.channel;

import io.neovim.java.Rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author dhleong
 */
public class FallbackChannel implements Rpc.Channel {

    final Rpc.Channel[] choices;
    Rpc.Channel chosen;

    public FallbackChannel(Rpc.Channel... choices) {
        if (choices.length == 0) {
            throw new IllegalArgumentException("You must provide at least one choice");
        }

        this.choices = choices;
    }

    @Override
    public void close() throws IOException {
        Rpc.Channel chosen = this.chosen;
        if (chosen != null) chosen.close();
    }

    @Override
    public void tryOpen() throws Exception {
        choose();
    }

    @Override
    public InputStream getInputStream() {
        return choose().getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        return choose().getErrorStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return choose().getOutputStream();
    }

    Rpc.Channel choose() {
        Rpc.Channel chosen = this.chosen;
        if (chosen != null) return chosen;

        Throwable lastError = null;
        for (Rpc.Channel choice : choices) {
            try {
                choice.getInputStream();
                return this.chosen = choice;
            } catch (Throwable e) {
                // nope
                lastError = e;

                try {
                    choice.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }

        throw new IllegalStateException("No channel choices worked", lastError);
    }
}
