package io.neovim.java;

import io.neovim.java.rpc.RequestPacket;
import io.reactivex.Single;

import javax.annotation.Nonnull;

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

    // NOTE: we don't use the standard setThing naming scheme
    // because the invocation is more intuitive this way (and
    // it returns a Single anyway, instead of void)
    public Single<Boolean> bufferSet(@Nonnull Buffer buffer) {
        return rpc.request(
            Boolean.class,
            RequestPacket.create("nvim_set_current_buf", buffer)
        );
    }

    public Single<Window> window() {
        return rpc.request(
            Window.class,
            RequestPacket.create("nvim_get_current_win")
        );
    }

    public Single<Tabpage> tabpage() {
        return rpc.request(
            Tabpage.class,
            RequestPacket.create("nvim_get_current_tabpage")
        );
    }

}
