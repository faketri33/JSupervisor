package org.faketri.net.handlers;

import org.faketri.net.ServerChannel;

import java.nio.channels.SocketChannel;


/**
 * Handles client connections accepted by a {@link ServerChannel}.
 *
 * <p>A connection handler is invoked when a new client connection is
 * accepted by the server and is responsible for processing that
 * connection.</p>
 */
public interface ConnectionHandler {

    /**
     * Handles an accepted client connection.
     *
     * @param channel the channel representing the client connection
     */
    void handle(SocketChannel channel);
}