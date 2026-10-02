package org.faketri.net.handlers;

import java.nio.channels.SocketChannel;


public interface ConnectionHandler {
    void handle(SocketChannel channel);
}
