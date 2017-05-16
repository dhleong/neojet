package io.neovim.java;

import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;
import io.reactivex.Single;

import java.io.Closeable;
import java.util.Arrays;

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
            RequestPacket.create("ui_attach",
                Arrays.asList(width, height, null))
        );
    }

    public static Neovim attachEmbedded() {
        return new Neovim(Rpc.createEmbedded());
    }

    public static Neovim attach(Rpc rpc) {
        return new Neovim(rpc);
    }

}
