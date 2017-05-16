package io.neovim.java.rpc;

/**
 * @author dhleong
 */
public class ResponsePacket extends Packet {
    public int requestId;
    public Object error;
    public Object result;

    private ResponsePacket() {
        type = Type.RESPONSE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ResponsePacket that = (ResponsePacket) o;

        if (requestId != that.requestId) return false;
        if (error != null ? !error.equals(that.error) : that.error != null) return false;
        return result != null ? result.equals(that.result) : that.result == null;
    }

    @Override
    public int hashCode() {
        int result1 = super.hashCode();
        result1 = 31 * result1 + requestId;
        result1 = 31 * result1 + (error != null ? error.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }

    public static ResponsePacket create(int requestId, Object error, Object result) {
        ResponsePacket packet = new ResponsePacket();
        packet.requestId = requestId;
        packet.error = error;
        packet.result = result;
        return packet;
    }
}
