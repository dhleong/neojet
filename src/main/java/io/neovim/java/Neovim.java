package io.neovim.java;

import io.neovim.java.rpc.NotificationPacket;
import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;
import io.reactivex.Flowable;
import io.reactivex.Single;

import javax.annotation.Nonnull;
import java.io.Closeable;

/**
 * @author dhleong
 */
public class Neovim implements Closeable {

    final Rpc rpc;

    private boolean hasQuit;

    protected Neovim(Rpc rpc) {
        this.rpc = rpc;
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
    public Single<ResponsePacket> command(String command) {
        return rpc.request(
            RequestPacket.create(
                "nvim_command",
                command
            )
        );
    }

    /**
     * Execute a single ex command and get the output
     */
    public Single<String> commandOutput(String command) {
        return rpc.request(
            String.class,
            RequestPacket.create(
                "nvim_command_output",
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
     * @return A Flowable of every NotificationPacket
     *  received ever
     */
    public Flowable<NotificationPacket> notifications() {
        return rpc.notifications();
    }

    /**
     * @return A Flowable of every NotificationPacket with the specified eventType
     */
    public Flowable<NotificationPacket> notifications(@Nonnull String eventType) {
        return notifications()
            .filter(notif -> eventType.equals(notif.event));
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
    public void uiAttach(int width, int height, boolean rgb) {
        rpc.sendRequest(
            RequestPacket.create(
                "ui_attach",
                width, height, rgb
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
