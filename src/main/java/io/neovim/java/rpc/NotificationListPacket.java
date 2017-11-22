package io.neovim.java.rpc;

import java.util.List;

/**
 * @author dhleong
 */
public class NotificationListPacket<T> extends NotificationPacket<List<T>> {

    public static <T> NotificationPacket<List<T>> create(String event, NotificationPacket<List<T>> p, List<T> args) {
        p.event = event;
        p.args = args;
        return p;
    }

}
