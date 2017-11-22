package io.neovim.java.rpc;

import io.neovim.java.event.Event;

/**
 * Base class for Notification RPC messages. Custom events
 *  can subclass this, but their argument type MUST either
 *  be a {@link java.util.List}—in which case, you should
 *  prefer to subclass {@link NotificationListPacket}—or be
 *  annotated with:
 *
 *  <code>
 *   {@literal @}JsonFormat(shape = JsonFormat.Shape.ARRAY)
 *  </code>
 *
 * @author dhleong
 */
public class NotificationPacket<T>
        extends Packet
        implements Event<T> {
    public String event;
    public T args;

    public NotificationPacket() {
        type = Type.NOTIFICATION;
    }

    @Override
    public String kind() {
        return event;
    }

    @Override
    public T value() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NotificationPacket that = (NotificationPacket) o;

        if (!event.equals(that.event)) return false;
        return args != null ? args.equals(that.args) : that.args == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + event.hashCode();
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NotificationPacket{" +
            "event='" + event + '\'' +
            ", args=" + args +
            '}';
    }

    public static <T> NotificationPacket<T> create(String event, T args) {
        return create(event, new NotificationPacket<>(), args);
    }

    public static <T> NotificationPacket<T> create(String event, NotificationPacket<T> p, T args) {
        p.event = event;
        p.args = args;
        return p;
    }
}
