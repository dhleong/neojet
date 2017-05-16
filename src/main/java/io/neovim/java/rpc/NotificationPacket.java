package io.neovim.java.rpc;

/**
 * @author dhleong
 */
public class NotificationPacket extends Packet {
    public String event;
    public Object args;

    private NotificationPacket() {
        type = Type.NOTIFICATION;
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


    public static NotificationPacket create(String event, Object args) {
        NotificationPacket p = new NotificationPacket();
        p.event = event;
        p.args = args;
        return p;
    }
}
