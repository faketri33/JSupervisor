package org.faketri.service;

import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationLifecycleImpl implements ApplicationLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ApplicationLifecycleImpl.class);
    private final ApplicationRegistry appRegistry;

    public ApplicationLifecycleImpl(ApplicationRegistry appRegistry) {
        this.appRegistry = appRegistry;
    }

    @Override
    public void start(Filters p) {
        log.debug("{}", p);
        appRegistry.find(p).ifPresentOrElse(ApplicationRuntime::start, () -> {
            throw new ApplicationException("Cannot find application for stop");
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
            throw new ApplicationException("Cannot find application for stop");
        });
    }
}
