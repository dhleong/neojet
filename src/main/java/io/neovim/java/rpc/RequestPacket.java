package io.neovim.java.rpc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author dhleong
 */
public class RequestPacket extends Packet {
    static int REQUEST_ID_UNSET = Integer.MIN_VALUE;

    public int requestId;
    public String method;
    public Object args;

    private RequestPacket() {
        type = Type.REQUEST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RequestPacket that = (RequestPacket) o;

        if (requestId != that.requestId) return false;
        if (!method.equals(that.method)) return false;
        return args != null ? args.equals(that.args) : that.args == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + requestId;
        result = 31 * result + method.hashCode();
        result = 31 * result + (args != null ? args.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RequestPacket{" +
            "requestId=" + requestId +
            ", method='" + method + '\'' +
            ", args=" + args +
            '}';
    }

    public static RequestPacket create(String method) {
        return create(method, Collections.emptyList());
    }
    public static RequestPacket create(String method, Object...args) {
        return create(method, Arrays.asList(args));
    }
    public static RequestPacket create(String method, List<?> args) {
        return create(REQUEST_ID_UNSET, method, args);
    }

    public static RequestPacket create(int requestId, String method, Object args) {
        RequestPacket packet = new RequestPacket();
        packet.requestId = requestId;
        packet.method = method;
        packet.args = args;
        return packet;
    }
}
