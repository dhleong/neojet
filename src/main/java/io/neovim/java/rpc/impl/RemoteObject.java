package io.neovim.java.rpc.impl;

import io.neovim.java.Rpc;

/**
 * @author dhleong
 */
public abstract class RemoteObject {

    private final Rpc rpc;

    protected RemoteObject(Rpc rpc) {
        this.rpc = rpc;
    }

//    protected Single<ResponsePacket> request(String type, Object... args) {
//        return nvim.
//    }
}
