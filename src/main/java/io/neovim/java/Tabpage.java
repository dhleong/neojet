package io.neovim.java;

import io.neovim.java.rpc.impl.RemoteObject;
import io.reactivex.Single;

/**
 * @author dhleong
 */
public class Tabpage extends RemoteObject {
    static final String API_PREFIX = "nvim_tabpage_";

    public Tabpage(Rpc rpc, long id) {
        super(rpc, API_PREFIX, id);
    }

    /**
     * @return The currently-focused Window on the tabpage
     */
    public Single<Window> window() {
        return request(
            Window.class,
            "nvim_tabpage_get_win"
        );
    }
}
