package io.neovim.java.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import io.neovim.java.event.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dhleong
 */
public class NotificationPacket<T>
        extends Packet
        implements Event<List<T>> {
    public String event;
    public List<T> args;

    protected NotificationPacket() {
        type = Type.NOTIFICATION;
    }

    @Override
    public String kind() {
        return event;
    }

    @Override
    public List<T> value() {
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

    public static <T> NotificationPacket<T> create(String event, List<T> args) {
        NotificationPacket<T> p = new NotificationPacket<>();
        p.event = event;
        p.args = args;
        return p;
    }

    public static NotificationPacket<JsonNode> createFromList(String event, JsonNode node) {
        final int size = node.size();
        ArrayList<JsonNode> result = new ArrayList<>(size);
        for (int i=0; i < size; ++i) {
            result.add(node.get(i));
        }
        return create(event, result);
    }
}
