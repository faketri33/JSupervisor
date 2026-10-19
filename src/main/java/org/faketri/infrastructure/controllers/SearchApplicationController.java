package org.faketri.infrastructure.controllers;


import org.faketri.domain.dto.AppSnapshot;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.io.dto.request.ProcessedRequest;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.service.ApplicationRegistry;

import java.util.Collection;

public final class SearchApplicationController implements Controller {
    private final ApplicationRegistry registry;

    public SearchApplicationController(ApplicationRegistry registry) {
        this.registry = registry;
    }

    public ResponseEntry<Collection<AppSnapshot>> ps(ProcessedRequest request) {
        return ResponseEntry.ok(registry.findAll(request.filters()).stream()
                .map(ApplicationRuntime::snapshot)
                .toList());
    }

    public void registerTo(RequestDispatcherCommand dispatcherCommand) {
        dispatcherCommand.registerNewDispatch("ps", this::ps);
    }
}
