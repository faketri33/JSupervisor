package org.faketri.net;

import org.faketri.net.handlers.CommandHandler;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.Response;

/**
 * Dispatches requests to registered {@link CommandHandler} instances based on the request type.
 */
public interface RequestDispatcherCommand {

    /**
     * Dispatches the specified request to the handler registered for its type.
     *
     * @param <Q> type of the request
     * @param <S> type of the response
     * @param req request to dispatch
     * @return response produced by the matching handler
     */
    <Q extends Request, S extends Response> S dispatch(Q req);

    /**
     * Registers a handler for the specified request type.
     *
     * @param <Q>     type of the request
     * @param <S>     type of the response
     * @param type    request type to associate with the handler
     * @param handler handler invoked for requests of the given type
     */
    <Q extends Request, S extends Response> void registerNewDispatch(Class<Q> type, CommandHandler<Q, S> handler);
}
