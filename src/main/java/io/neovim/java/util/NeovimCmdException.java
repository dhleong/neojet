package io.neovim.java.util;

import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.ResponsePacket;

/**
 * An exception raised as a result of executing
 *  a command (specifically, sending a request)
 *
 * @author dhleong
 */
public class NeovimCmdException extends Exception {
    public final RequestPacket request;
    public final ResponsePacket response;

    public NeovimCmdException(RequestPacket request, ResponsePacket response) {
        super(buildMessage(request, response));

        this.request = request;
        this.response = response;
    }

    private static String buildMessage(RequestPacket request, ResponsePacket response) {
        return "Error executing request:\n" +
            request +
            "\nError response: " +
            response.error.toString();
    }
}
