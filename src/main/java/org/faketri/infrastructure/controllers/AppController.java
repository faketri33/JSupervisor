package org.faketri.infrastructure.controllers;

import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.handlers.ResponseEntry;
import org.faketri.net.io.dto.request.AllRequest;
import org.faketri.net.io.dto.request.RunRequest;
import org.faketri.net.io.dto.response.AppsResponse;
import org.faketri.service.ApplicationService;


public class AppController {

    private final ApplicationService service;

    public AppController(ApplicationService service) {
        this.service = service;
    }

    public AppsResponse getAll(AllRequest request) {
        return new AppsResponse(service.getActive());
    }

    public void run(RunRequest request) {
        service.start(request.app());
    }

    public void registerTo(RequestDispatcherCommand dispatcherCommand) {
        dispatcherCommand.registerNewDispatch(AllRequest.class, this::getAll);
        dispatcherCommand.registerNewDispatch(RunRequest.class, ResponseEntry.ok(this::run));
    }
}
