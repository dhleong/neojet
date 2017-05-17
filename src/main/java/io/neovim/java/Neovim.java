package io.neovim.java;

import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;
import io.reactivex.Single;

import java.io.Closeable;

/**
 * @author dhleong
 */
public class Neovim implements Closeable {

    final Rpc rpc;

    protected Neovim(Rpc rpc) {
        this.rpc = rpc;
    }

    @Override
    public void close() {
        rpc.close();
    }

    /**
     * Execute a single ex command
     */
    public void command(String command) {
        rpc.sendRequest(
            RequestPacket.create(
                "nvim_command",
                command
            )
        );
    }

    public Single<ResponsePacket> getApiInfo() {
        return rpc.request(
            RequestPacket.create("vim_get_api_info")
        );
    }

    /**
     * Register as a remote UI
     */
    public void uiAttach(int width, int height, int rgb) {
        rpc.sendRequest(
            RequestPacket.create(
                "ui_attach",
                width, height, null
            )
        );
    }

    /**
     * Unregister as remote UI
     */
    public void uiDetach() {
        rpc.sendRequest(RequestPacket.create("ui_detach"));
    }

    public static Neovim attachEmbedded() {
        return attach(Rpc.createEmbedded());
    }

    public static Neovim attach(Rpc rpc) {
        return new Neovim(rpc);
    }
}
