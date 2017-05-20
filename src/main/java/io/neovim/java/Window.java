package io.neovim.java;

import io.neovim.java.rpc.impl.RemoteObject;
import io.reactivex.Single;

import javax.annotation.CheckReturnValue;

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

    /**
     * Get the {@link Tabpage} containing this window
     */
    public Single<Tabpage> tabpage() {
        return request(
            Tabpage.class,
            "nvim_win_get_tabpage"
        );
    }

    /**
     * @return an IntPair representing the dimensions of this window
     */
    public Single<IntPair> cursor() {
        return request(
            IntPair.class,
            "nvim_win_get_cursor"
        );
    }

    /**
     * @return an IntPair representing the on-screen window position
     *  (row, col) in display cells
     */
    @CheckReturnValue
    public Single<IntPair> position() {
        return request(
            IntPair.class,
            "nvim_win_get_position"
        );
    }

    @CheckReturnValue
    public Single<Boolean> setCursor(IntPair newCursor) {
        return request(
            Boolean.class,
            "nvim_win_set_cursor",
            newCursor
        );
    }

    @CheckReturnValue
    public Single<Boolean> setCursor(int row, int col) {
        return setCursor(new IntPair(row, col));
    }

    @CheckReturnValue
    public Single<Integer> height() {
        return request(
            Integer.class,
            "nvim_win_get_height"
        );
    }

    @CheckReturnValue
    public Single<Boolean> setHeight(int height) {
        return request(
            Boolean.class,
            "nvim_win_set_height",
            height
        );
    }

    @CheckReturnValue
    public Single<Integer> width() {
        return request(
            Integer.class,
            "nvim_win_get_width"
        );
    }

    @CheckReturnValue
    public Single<Boolean> setWidth(int width) {
        return request(
            Boolean.class,
            "nvim_win_set_width",
            width
        );
    }
}
