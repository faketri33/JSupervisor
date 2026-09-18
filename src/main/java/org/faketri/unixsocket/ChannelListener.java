package org.faketri.unixsocket;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ChannelListener extends Closeable, AutoCloseable {
    void listen(SocketChannel channel, RequestHandler handler) throws IOException;
}
