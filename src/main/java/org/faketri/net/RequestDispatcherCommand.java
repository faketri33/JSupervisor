package org.faketri.net;

import org.faketri.infrastructure.commands.Command;
import org.faketri.infrastructure.filter.QueryFactory;
import org.faketri.net.handlers.CommandHandler;
import org.faketri.net.io.dto.request.DispatchableRequest;
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
    ResponseEntry<?> dispatch(DispatchableRequest req);

    boolean verifyRoutes();

    /**
     * Registers a handler for the specified request type.
     *
     * @param command request type to associate with the handler
     * @param handler handler invoked for requests of the given type
     */
    <T> void register(Command command, CommandHandler<T> handler);

    <T> void register(Command command, CommandHandler<T> handler, QueryFactory<T> queries);
}
