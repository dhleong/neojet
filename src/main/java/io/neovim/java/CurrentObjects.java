package io.neovim.java;

import io.neovim.java.rpc.RequestPacket;
import io.reactivex.Single;

/**
 * @author dhleong
 */
public class CurrentObjects {
    private final Rpc rpc;

    public CurrentObjects(Rpc rpc) {
        this.rpc = rpc;
    }

    public Single<Buffer> buffer() {
        return rpc.request(
            Buffer.class,
            RequestPacket.create("nvim_get_current_buf")
        );
    }
}
