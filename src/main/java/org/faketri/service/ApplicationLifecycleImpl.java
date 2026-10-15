package org.faketri.service;

import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.Filters;

public class ApplicationLifecycleImpl implements ApplicationLifecycle {
    private final ApplicationRegistry appRegistry;

    public ApplicationLifecycleImpl(ApplicationRegistry appRegistry) {
        this.appRegistry = appRegistry;
    }

    @Override
    public void start(Filters p) {
        appRegistry.find(p).ifPresentOrElse(ApplicationRuntime::start, () -> {
            throw new ApplicationException("Cannot find application for stop " + p);
        });
    }

    @Override
    public void restart(Filters p) {
        stop(p);
        start(p);
    }

    @Override
    public void stop(Filters p) {
        appRegistry.find(p).ifPresentOrElse(ApplicationRuntime::stop, () -> {
            throw new ApplicationException("Cannot find application for stop" + p);
        });
    }
}
