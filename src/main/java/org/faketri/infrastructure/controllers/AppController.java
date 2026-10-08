package org.faketri.infrastructure.controllers;

import org.faketri.domain.dto.ApplicationView;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.service.ApplicationService;

import java.util.Collection;


public class AppController implements Controller {

    private final ApplicationService service;

    public AppController(ApplicationService service) {
        this.service = service;
    }

    public ResponseEntry<Collection<ApplicationView>> getAll(Request request) {
        return ResponseEntry.ok(service.getAll());
    }

    public ResponseEntry<Collection<ApplicationView>> getAllActive(Request req) {
        return ResponseEntry.ok(service.getActive());
    }

    public void run(Request request) {
        if (request.args().length == 0) return;
        service.start(request.args()[0]);
    }

    public void registerTo(RequestDispatcherCommand dispatcherCommand) {
        dispatcherCommand.registerNewDispatch("all", this::getAll);
        dispatcherCommand.registerNewDispatch("allActive", this::getAllActive);
        dispatcherCommand.registerNewDispatch("run", req -> {
            this.run(req);
            return ResponseEntry.ok();
        });
    }
}
