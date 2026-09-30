package org.faketri.net.socket.handlers;

import org.faketri.infrastructure.exceptions.unixserver.BadRequestException;
import org.faketri.net.socket.dto.request.Request;
import org.faketri.net.socket.dto.response.Response;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private final Map<Class<? extends Request>, CommandHandler<? extends Request, ? extends Response>> routes = new HashMap<>();

    public void register(Class<? extends Request> type, CommandHandler<? extends Request, ? extends Response> handler) {
        routes.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public Response dispatch(Request req) {
        var h = (CommandHandler<Request, Response>) routes.get(req.getClass());
        if (h == null) throw new BadRequestException("UNKNOWN COMMAND " + req.getClass().getSimpleName());
        return h.handle(req);
    }
}
