package org.faketri.infrastructure.controllers;

import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.commands.Command;
import org.faketri.infrastructure.filter.QueryFactory;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.RuntimeField;
import org.faketri.net.EStatusCode;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.request.RequestContext;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.net.socket.mapper.RequestExceptionMapper;
import org.faketri.service.ApplicationLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class LifecycleController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(LifecycleController.class);
    private final ApplicationLifecycle lifecycle;

    public LifecycleController(ApplicationLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public ResponseEntry<?> start(RequestContext<ApplicationRuntime> request) {
        log.debug("Start application");
        return saveExecute(() -> lifecycle.start(request.query()));
    }

    public ResponseEntry<?> stop(RequestContext<ApplicationRuntime> request) {
        log.debug("Stop application");
        return saveExecute(() -> lifecycle.stop(request.query()));
    }

    public ResponseEntry<?> restart(RequestContext<ApplicationRuntime> request) {
        log.debug("Restart application");
        return saveExecute(() -> lifecycle.restart(request.query()));
    }

    private ResponseEntry<?> saveExecute(Runnable action) {
        try {
            action.run();
            log.debug("Cancel work for task");
            return ResponseEntry.ok();
        } catch (ApplicationException ex) {
            return RequestExceptionMapper.map(new ErrorRequest(ex.getMessage(), EStatusCode.BAD_REQUEST));
        }
    }


    @Override
    public void register(RequestDispatcherCommand dispatcherCommand) {
        var runtimeQueries = new QueryFactory<>(List.of(RuntimeField.values()));

        dispatcherCommand.register(Command.RUN, this::start, runtimeQueries);
        dispatcherCommand.register(Command.STOP, this::stop, runtimeQueries);
        dispatcherCommand.register(Command.RESTART, this::restart, runtimeQueries);
    }
}
