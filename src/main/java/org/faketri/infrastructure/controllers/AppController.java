package org.faketri.infrastructure.controllers;

import org.faketri.net.ServerChannel;
import org.faketri.net.io.dto.request.AllRequest;
import org.faketri.net.io.dto.request.RunRequest;
import org.faketri.net.io.dto.response.AppInfo;
import org.faketri.net.io.dto.response.AppsResponse;
import org.faketri.net.io.dto.response.OkResponse;
import org.faketri.service.ApplicationService;

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

    public OkResponse run(RunRequest request) {
        service.startByName(request.app());
        return new OkResponse();
    }

    public void registerTo(ServerChannel channel) {
        channel.customHandler(AllRequest.class, this::getAll);
        channel.customHandler(RunRequest.class, this::run);
    }
}
