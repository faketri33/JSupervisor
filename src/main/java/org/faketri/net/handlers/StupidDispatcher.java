package org.faketri.net.handlers;

import org.faketri.infrastructure.commands.Command;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.exceptions.request.BadRequestException;
import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.request.ProcessedRequest;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.net.socket.mapper.RequestExceptionMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class StupidDispatcher implements RequestDispatcherCommand {
    private final Map<Command, CommandHandler> routes = new HashMap<>();

    public ResponseEntry<?> dispatch(ProcessedRequest req) {
        var h = routes.get(req.command());
        try {
            if (h == null) throw new BadRequestException("Unknown command " + req.command());
            return h.handle(req);
        } catch (ErrorRequest e) {
            return RequestExceptionMapper.map(e);
        } catch (Exception e) {
            return RequestExceptionMapper.map(e);
        }
    }

    @Override
    public void registerNewDispatch(Command command, CommandHandler handler) {
        if (command == null)
            throw new IllegalArgumentException("Command address cannot be empty");
        this.routes.put(command, Objects.requireNonNull(handler, "Handler cannot be null"));
    }
}
