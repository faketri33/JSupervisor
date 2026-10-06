package org.faketri.infrastructure.controllers;

import org.faketri.net.RequestDispatcherCommand;


/**
 * Represents a controller that connects a server with a service responsible
 * for processing user requests.
 *
 * <p>A controller defines which request handler should process a particular
 * request. It registers this mapping with a {@link RequestDispatcherCommand},
 * allowing the server to dispatch incoming requests to the appropriate
 * controller.</p>
 *
 * <p>Each controller must register its request handler with the dispatcher
 * before it can receive dispatched requests.</p>
 */
public interface Controller {

    /**
     * Registers this controller's request handler with the dispatcher.
     *
     * <p>The dispatcher uses the registered handler to route incoming
     * requests to the appropriate controller.</p>
     *
     * @param dispatcherCommand dispatcher responsible for routing requests
     *                          to registered controllers
     */
    void registerTo(RequestDispatcherCommand dispatcherCommand);
}
