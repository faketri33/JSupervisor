package org.faketri.net.handlers;

import org.faketri.infrastructure.commands.Command;
import org.faketri.infrastructure.filter.Query;
import org.faketri.infrastructure.filter.QueryFactory;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.exceptions.request.BadRequestException;
import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.request.DispatchableRequest;
import org.faketri.net.io.dto.request.RequestContext;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.net.socket.mapper.RequestExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class StupidDispatcher implements RequestDispatcherCommand {

    private final static Logger log = LoggerFactory.getLogger(StupidDispatcher.class);
    private final Map<Command, RouteProcessor> routes = new HashMap<>();

    public ResponseEntry<?> dispatch(DispatchableRequest req) {
        try {
            var h = routes.get(req.command());
            if (h == null) throw new BadRequestException("Unknown command " + req.command());
            return h.process(req);
        } catch (ErrorRequest e) {
            return RequestExceptionMapper.map(e);
        } catch (Exception e) {
            return RequestExceptionMapper.map(e);
        }
    }

    @Override
    public boolean verifyRoutes() {
        boolean verified = true;
        Collection<String> pathNotRoutes = new ArrayList<>();
        for (Command c : Command.values())
            if (!routes.containsKey(c)) {
                verified = false;
                pathNotRoutes.add(c.name());
            }
        log.debug("Routes without path - {}", pathNotRoutes);
        return verified;
    }

    private void add(Command command, RouteProcessor routeProcessor) {
        if (routes.putIfAbsent(command, routeProcessor) != null)
            throw new BadRequestException("Unknown command " + command);
    }

    @Override
    public <T> void register(Command command, CommandHandler<T> handler) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(handler);

        if (command.target() != Command.Target.NONE)
            throw new IllegalArgumentException(command + " needs a target, register it with a QueryFactory");

        add(command, req -> {
            RequestContext<T> context = new RequestContext<>(req.args(), Query.all());
            return handler.handle(context);
        });
    }

    @Override
    public <T> void register(Command command, CommandHandler<T> handler, QueryFactory<T> queries) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(handler);
        Objects.requireNonNull(queries);
        add(command, req ->
                handler.handle(new RequestContext<>(req.args(), queries.parse(req.conditions())))
        );
    }

    private interface RouteProcessor {
        ResponseEntry<?> process(DispatchableRequest req) throws Exception;
    }
}
