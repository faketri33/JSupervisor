package org.faketri.net;

import org.faketri.net.handlers.CommandHandler;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.ResponseEntry;

/**
 * Dispatches requests to registered {@link CommandHandler} instances based on the request type.
 */
public interface RequestDispatcherCommand {

    /**
     * Dispatches the specified request to the handler registered for its type.
     *
     * @param req request to dispatch
     * @return response produced by the matching handler
     */
    ResponseEntry<?> dispatch(Request req);

    /**
     * Registers a handler for the specified request type.
     *
     * @param path    request type to associate with the handler
     * @param handler handler invoked for requests of the given type
     */
    void registerNewDispatch(String path, CommandHandler handler);
}
