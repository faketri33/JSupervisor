package org.faketri.infrastructure.controllers;


import org.faketri.domain.dto.AppSnapshot;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.RuntimeFilters;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.io.dto.request.Args;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.ResponseEntry;
import org.faketri.service.ApplicationRegistry;

import java.util.Collection;
import java.util.function.Predicate;

public final class SearchApplicationController implements Controller {
    private final ApplicationRegistry registry;

    public SearchApplicationController(ApplicationRegistry registry) {
        this.registry = registry;
    }

    public ResponseEntry<Collection<AppSnapshot>> snapshots(Request request) {
        Args args = request.args();

        Predicate<ApplicationRuntime> filter = RuntimeFilters.parse(args.values("filter"));
        boolean statusGiven = args.values("filter").stream().anyMatch(f -> f.startsWith("status="));
        if (!args.flag("a", "all") && !statusGiven) {
            filter = filter.and(ApplicationRuntime::isAlive);
        }

        return ResponseEntry.ok(registry.findAll(filter).stream()
                .map(ApplicationRuntime::snapshot)
                .toList());
    }


    public void registerTo(RequestDispatcherCommand dispatcherCommand) {
        dispatcherCommand.registerNewDispatch("ps", this::snapshots);
    }
}
