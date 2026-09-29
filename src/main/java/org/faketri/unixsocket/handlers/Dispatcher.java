package org.faketri.unixsocket.handlers;

import org.faketri.infrastructure.exceptions.unixserver.ErrorRequest;
import org.faketri.unixsocket.dto.request.Request;
import org.faketri.unixsocket.dto.response.Response;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private final Map<Class<? extends Request>, CommandHandler<? extends Request, ? extends Response>> routes = new HashMap<>();

    public void register(Class<? extends Request> type, CommandHandler<? extends Request, ? extends Response> handler) {
        routes.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public Response dispatch(Request req) throws Exception {
        var h = (CommandHandler<Request, Response>) routes.get(req.getClass());
        if (h == null) throw new ErrorRequest("UNKNOWN COMMAND " + req.getClass().getSimpleName());
        return h.handle(req);
    }
}
