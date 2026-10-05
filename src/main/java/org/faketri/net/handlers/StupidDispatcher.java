package org.faketri.net.handlers;

import org.faketri.infrastructure.exceptions.unixserver.BadRequestException;
import org.faketri.infrastructure.exceptions.unixserver.ErrorRequest;
import org.faketri.net.EStatusCode;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.ErrorResponse;
import org.faketri.net.io.dto.response.Response;
import org.faketri.net.socket.mapper.RequestExceptionMapper;

import java.util.HashMap;
import java.util.Map;

public class StupidDispatcher implements RequestDispatcherCommand {
    private final Map<Class<? extends Request>, CommandHandler<? extends Request, ? extends Response>> routes = new HashMap<>();

    @SuppressWarnings("unchecked")
    public Response dispatch(Request req) {
        var h = (CommandHandler<Request, Response>) routes.get(req.getClass());
        if (h == null) throw new BadRequestException("UNKNOWN COMMAND " + req.getClass().getSimpleName());

        try {
            return h.handle(req);
        } catch (ErrorRequest e) {
            return RequestExceptionMapper.map(e);
        } catch (Exception e) {
            return RequestExceptionMapper.map(e);
        }
    }

    @Override
    public <Q extends Request, S extends Response> void registerNewDispatch(Class<Q> type, CommandHandler<Q, S> handler) {
        this.routes.put(type, handler);
    }
}
