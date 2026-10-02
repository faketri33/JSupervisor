package org.faketri.net;

import org.faketri.net.socket.UnixServerSocket;

public class ServerFactory {

    private ServerFactory() {
    }

    public static ServerChannel newUnixSocketServer() {
        return UnixServerSocket.of();
    }

    public static ServerChannel newUnixSocketServer(RequestDispatcherCommand dispatcherCommand) {
        return UnixServerSocket.of(dispatcherCommand);
    }
}
