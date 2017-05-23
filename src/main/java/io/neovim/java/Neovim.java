package io.neovim.java;

import io.neovim.java.rpc.NotificationPacket;
import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;
import io.neovim.java.rpc.channel.SocketChannel;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.io.Closeable;

/**
 * @author dhleong
 */
public class Neovim implements Closeable {

    public final CurrentObjects current;

    final Rpc rpc;

    private boolean hasQuit;

    protected Neovim(Rpc rpc) {
        this.rpc = rpc;

        current = new CurrentObjects(rpc);
    }

    @Override
    public void close() {
        if (!hasQuit) {
            quit();
        }
        rpc.close();
    }

    /**
     * Execute a single ex command
     */
    @CheckReturnValue
    public Single<Boolean> command(String command) {
        return rpc.request(
            Boolean.class,
            RequestPacket.create(
                "nvim_command",
                command
            )
        );
    }

    /**
     * Execute a single ex command and get the output
     */
    @CheckReturnValue
    public Single<String> commandOutput(String command) {
        return rpc.request(
            String.class,
            RequestPacket.create(
                "nvim_command_output",
                command
            )
        );
    }

    /**
     * Push `keys` to Nvim user input buffer.
     from a mapping. This matters for undo, opening folds, etc.
     * @param keys A string of keys
     * @param options A string with the following character flags:
     *  - 'm': Remap keys. This is default.
     *  - 'n': Do not remap keys.
     *  - 't': Handle keys as if typed; otherwise they are handled as if coming
     *         from a mapping. This matters for undo, opening folds, etc.
     */
    public Single<Boolean> feedKeys(String keys, String options) {
        return feedKeys(keys, options, true);
    }
    /** @see #feedKeys(String, String) */
    public Single<Boolean> feedKeys(String keys, String options, boolean escapeCSI) {
        return rpc.request(
            Boolean.class,
            RequestPacket.create(
                "nvim_feedkeys",
                keys, options, escapeCSI
            )
        );
    }

    /**
     * Push `bytes` to Nvim low level input buffer.
     *
     * Unlike `feedkeys()`, this uses the lowest level input buffer and the
     * call is not deferred.
     * @return the number of bytes actually written (which can be less than
     *  what was requested if the buffer is full).
     */
    public Single<Integer> input(String bytes) {
        return rpc.request(
            Integer.class,
            RequestPacket.create(
                "nvim_input",
                bytes
            )
        );
    }

    public Single<ResponsePacket> getApiInfo() {
        return rpc.request(
            RequestPacket.create("vim_get_api_info")
        );
    }

    /**
     * @return A Flowable of every NotificationPacket
     *  received ever
     */
    public Flowable<NotificationPacket> notifications() {
        return rpc.notifications();
    }

    /**
     *
     * @return A Flowable of the args to every NotificationPacket
     *  with the specified eventType
     */
    public <R, T extends NotificationPacket<R>> Flowable<R> notifications(
            @Nonnull Class<T> eventType) {
        String eventName = rpc.eventsManager.getEventName(eventType);

        //noinspection unchecked
        return notifications()
            .filter(notif -> eventName.equals(notif.event))
            .map(notif -> ((T) notif).value());
    }

    public void quit() {
        quit("qa!");
    }
    public void quit(@Nonnull String quitCommand) {
        hasQuit = true;
        try {
            command(quitCommand).blockingGet();
        } catch (Throwable e) {
            // this is normal; quitting closes the process.
            // We use the blockingGet to make sure we wait
            // until that's done.
        }
    }

    /**
     * Register as a remote UI
     */
    @CheckReturnValue
    public Single<Boolean> uiAttach(int width, int height, boolean rgb) {
        return rpc.request(
            Boolean.class,
            RequestPacket.create(
                "ui_attach",
                width, height, rgb
            )
        );
    }

    /**
     * Unregister as remote UI
     */
    @CheckReturnValue
    public Single<Boolean> uiDetach() {
        return rpc.request(
            Boolean.class,
            RequestPacket.create("ui_detach")
        );
    }

    /*
     Factory methods
     */

    /**
     * Create a Neovim session attached to an embedded Neovim
     */
    public static Neovim attachEmbedded() {
        return attach(Rpc.createEmbedded());
    }

    /**
     * Create a Neovim session attached to a running Neovim
     *   via a socket connection
     */
    public static Neovim attachSocket(String host, int port) {
        return attach(Rpc.create(new SocketChannel(host, port)));
    }

    /**
     * Create a Neovim session attached to the provided Rpc session
     */
    public static Neovim attach(Rpc rpc) {
        return new Neovim(rpc);
    }
}
