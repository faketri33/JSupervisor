package org.faketri.net;

import org.faketri.net.socket.UnixServerSocket;

public class ServerFactory {

    private ServerFactory() {
    }

    public static ServerChannel newUnixSocketServer() {
        return UnixServerSocket.of();
    }
}
