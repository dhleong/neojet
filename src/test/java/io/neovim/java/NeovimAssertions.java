package io.neovim.java;

import io.neovim.java.rpc.NotificationPacket;
import io.neovim.java.rpc.NotificationPacketAssert;
import io.neovim.java.rpc.Packet;
import io.neovim.java.rpc.PacketAssert;
import io.neovim.java.rpc.RequestPacket;
import io.neovim.java.rpc.RequestPacketAssert;
import io.neovim.java.rpc.ResponsePacket;
import io.neovim.java.rpc.ResponsePacketAssert;
import org.assertj.core.api.Assertions;

/**
 * @author dhleong
 */
public class NeovimAssertions extends Assertions {

    public static IntPairAssert assertThat(IntPair actual) {
        return new IntPairAssert(actual);
    }

    public static PacketAssert assertThat(Packet actual) {
        return new PacketAssert(actual);
    }

    public static NotificationPacketAssert assertThat(NotificationPacket actual) {
        return new NotificationPacketAssert(actual);
    }

    public static RequestPacketAssert assertThat(RequestPacket actual) {
        return new RequestPacketAssert(actual);
    }

    public static ResponsePacketAssert assertThat(ResponsePacket actual) {
        return new ResponsePacketAssert(actual);
    }
}
