package io.neovim.java;

import io.neovim.java.rpc.impl.RemoteObject;
import io.reactivex.Single;

/**
 * @author dhleong
 */
public class Buffer extends RemoteObject {
    static String API_PREFIX = "nvim_buf_";

    public Buffer(Rpc rpc, long id) {
        super(rpc, API_PREFIX, id);
    }

    public Single<String> name() {
        return request(String.class, "nvim_buf_get_name");
    }

}
