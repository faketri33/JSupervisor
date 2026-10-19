package org.faketri.infrastructure.controllers;

import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.commands.Command;
import org.faketri.net.EStatusCode;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.request.ProcessedRequest;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.net.socket.mapper.RequestExceptionMapper;
import org.faketri.service.ApplicationLifecycle;


public class LifecycleController implements Controller {
    private final ApplicationLifecycle lifecycle;

    public LifecycleController(ApplicationLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public ResponseEntry<?> start(ProcessedRequest request) {
        return saveExecute(() -> lifecycle.start(request.filters()));
    }

    public ResponseEntry<?> stop(ProcessedRequest request) {
        return saveExecute(() -> lifecycle.stop(request.filters()));
    }

    public ResponseEntry<?> restart(ProcessedRequest request) {
        return saveExecute(() -> lifecycle.restart(request.filters()));
    }

    private ResponseEntry<?> saveExecute(Runnable action) {
        try {
            action.run();
            return ResponseEntry.ok();
        } catch (ApplicationException ex) {
            return RequestExceptionMapper.map(new ErrorRequest(ex.getMessage(), EStatusCode.BAD_REQUEST));
        }
    }


    @Override
    public void registerTo(RequestDispatcherCommand dispatcherCommand) {
        dispatcherCommand.registerNewDispatch(Command.RUN, this::start);
        dispatcherCommand.registerNewDispatch(Command.STOP, this::stop);
        dispatcherCommand.registerNewDispatch(Command.RESTART, this::restart);
    }
}
