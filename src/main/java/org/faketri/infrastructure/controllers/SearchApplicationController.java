package org.faketri.infrastructure.controllers;


import org.faketri.domain.dto.AppSnapshot;
import org.faketri.infrastructure.commands.Command;
import org.faketri.infrastructure.filter.Query;
import org.faketri.infrastructure.filter.QueryFactory;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.RuntimeField;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.io.dto.request.RequestContext;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.service.ApplicationRegistry;

import java.util.Collection;
import java.util.List;

public final class SearchApplicationController implements Controller {
    private static final Query<ApplicationRuntime> ALIVE = Query.named("alive", ApplicationRuntime::isAlive);
    private final ApplicationRegistry registry;

    public SearchApplicationController(ApplicationRegistry registry) {
        this.registry = registry;
    }

    public ResponseEntry<Collection<AppSnapshot>> ps(RequestContext<ApplicationRuntime> request) {

        Query<ApplicationRuntime> q = request.query();
        if (!request.args().flag("a", "all") && !q.uses(RuntimeField.STATUS)) q = q.and(ALIVE);

        return ResponseEntry.ok(registry.findAll(request.query()).stream()
                .map(ApplicationRuntime::snapshot)
                .toList());
    }

    public void register(RequestDispatcherCommand dispatcherCommand) {
        var quiresFactory = new QueryFactory<>(List.of(RuntimeField.values()));
        dispatcherCommand.register(Command.PS, this::ps, quiresFactory);
    }
}
