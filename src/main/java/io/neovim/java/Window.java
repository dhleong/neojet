package io.neovim.java;

import io.neovim.java.rpc.impl.RemoteObject;
import io.reactivex.Single;

/**
 * @author dhleong
 */
public class Window extends RemoteObject {
    static final String API_PREFIX = "nvim_win_";

    public Window(Rpc rpc, long id) {
        super(rpc, API_PREFIX, id);
    }

    public Single<Buffer> buffer() {
        return request(
            Buffer.class,
            "nvim_win_get_buf"
        );
    }
}
