package org.faketri.net.handlers;

import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.exceptions.request.BadRequestException;
import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.net.socket.mapper.RequestExceptionMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class StupidDispatcher implements RequestDispatcherCommand {
    private final Map<String, CommandHandler> routes = new HashMap<>();

    public ResponseEntry<?> dispatch(Request req) {
        var h = routes.get(req.command());

        try {
            if (h == null) throw new BadRequestException("UNKNOWN COMMAND " + req.command());
            return h.handle(req);
        } catch (ErrorRequest e) {
            return RequestExceptionMapper.map(e);
        } catch (Exception e) {
            return RequestExceptionMapper.map(e);
        }
    }

    @Override
    public void registerNewDispatch(String mainCommand, CommandHandler handler) {
        Objects.requireNonNull(mainCommand);
        Objects.requireNonNull(handler);
        if (mainCommand.isBlank()) throw new UnsupportedOperationException("Command address cannot be empty");
        this.routes.put(mainCommand, handler);
    }
}
