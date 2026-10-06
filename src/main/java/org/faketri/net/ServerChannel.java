package org.faketri.net;


/**
 * Represents a server-side communication channel responsible for accepting
 * client connections.
 *
 * <p>Implementations define the underlying transport and how accepted
 * connections are passed for further processing.</p>
 */
public interface ServerChannel {

    /**
     * Starts the server and begins accepting client connections.
     *
     * <p>This method blocks while the server is running and accepting
     * connections. The implementation is responsible for managing the
     * server channel and dispatching accepted client connections for
     * processing.</p>
     */
    void start();
}