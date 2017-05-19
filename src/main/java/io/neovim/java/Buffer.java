package io.neovim.java;

import io.neovim.java.rpc.impl.RemoteObject;
import io.reactivex.Single;

import javax.annotation.CheckReturnValue;
import java.util.List;

/**
 * @author dhleong
 */
public class Buffer extends RemoteObject {
    static String API_PREFIX = "nvim_buf_";

    public Buffer(Rpc rpc, long id) {
        super(rpc, API_PREFIX, id);
    }

    @CheckReturnValue
    public Single<Boolean> append(List<String> lines) {
        return insert(lines, -1);
    }

    @CheckReturnValue
    public Single<Boolean> insert(List<String> lines, int atIndex) {
        return setLines(atIndex, atIndex, false, lines);
    }

    public Single<String> line(int index) {
        return lines(index, index + 1, true).get(0);
    }

    public Lines lines() {
        return lines(0, -1, false);
    }

    public Lines lines(int start) {
        return lines(start, -1, false);
    }

    public Lines lines(int start, int end) {
        return lines(start, end, false);
    }

    public Single<String> name() {
        return request(String.class, "nvim_buf_get_name");
    }

    Single<List<String>> fetchLines(int start, int end, boolean strictIndexing) {
        //noinspection unchecked
        return request(
            Object.class,
            "nvim_buf_get_lines",
            start, end, strictIndexing
        ).map(o -> (List<String>) o);
    }

    @CheckReturnValue
    Single<Boolean> setLines(int start, int end, boolean strictIndexing, List<String> replacement) {
        return request(
            Boolean.class,
            "nvim_buf_set_lines",
            start, end, strictIndexing, replacement
        );
    }

    private Lines lines(int start, int end, boolean strictIndexing) {
        return new Lines(this, start, end, strictIndexing);
    }
}
