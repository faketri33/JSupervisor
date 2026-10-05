package org.faketri.infrastructure.controllers;

import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.handlers.ResponseEntry;
import org.faketri.net.io.dto.request.AllRequest;
import org.faketri.net.io.dto.request.RunRequest;
import org.faketri.net.io.dto.response.AppInfo;
import org.faketri.net.io.dto.response.AppsResponse;
import org.faketri.net.io.dto.response.LogResponse;
import org.faketri.net.io.dto.response.OkResponse;
import org.faketri.service.ApplicationService;
import org.faketri.utils.Constants;

public class AppController {

    private final ApplicationService service;

    public AppController(ApplicationService service) {
        this.service = service;
    }

    public AppsResponse getAll(AllRequest request) {
        return new AppsResponse(service.getAll()
                .stream()
                .map(a -> new AppInfo(a.getName(), a.getAppId().toString(), -1, a.getConfiguration().getCommands()))
                .toList());
    }

    public void run(RunRequest request) {
        service.start(request.app());
    }

    public void registerTo(RequestDispatcherCommand dispatcherCommand) {
        dispatcherCommand.registerNewDispatch(AllRequest.class, this::getAll);
        dispatcherCommand.registerNewDispatch(RunRequest.class, ResponseEntry.ok(this::run));
    }
}
