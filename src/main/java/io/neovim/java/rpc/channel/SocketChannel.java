package io.neovim.java.rpc.channel;

import io.neovim.java.Rpc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author dhleong
 */
public class SocketChannel implements Rpc.Channel {

    private final InetSocketAddress address;

    Socket socket;

    public SocketChannel(@Nonnull InetSocketAddress address) {
        this.address = address;
    }

    public SocketChannel(@Nullable String host, int port) {
        this(host == null
            ? new InetSocketAddress((InetAddress) null, port)
            : new InetSocketAddress(host, port));
    }

    @Override
    public void tryOpen() throws Exception {
        socket();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return socket().getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public InputStream getErrorStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return socket().getOutputStream();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws IOException {
        Socket socket = this.socket;
        if (socket != null) {
            socket.close();
        }
        this.socket = null;
    }

    private Socket socket() {
        Socket existing = socket;
        if (existing != null) return existing;
        try {
            return socket = new Socket(address.getAddress(), address.getPort());
        } catch (IOException e) {
            throw new IllegalArgumentException(
                "Unable to connect to nvim at " + address, e);
        }
    }
}
