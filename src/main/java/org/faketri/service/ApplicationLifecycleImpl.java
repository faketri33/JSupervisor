package org.faketri.service;

import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.process.ApplicationRuntime;

import java.util.function.Predicate;

public class ApplicationLifecycleImpl implements ApplicationLifecycle {
    private final ApplicationRegistry appRegistry;

    public ApplicationLifecycleImpl(ApplicationRegistry appRegistry) {
        this.appRegistry = appRegistry;
    }

    @Override
    public void start(Predicate<ApplicationRuntime> p) {
        appRegistry.find(p).ifPresentOrElse(ApplicationRuntime::start, () -> {
            throw new ApplicationException("");
        });
    }

    @Override
    public void restart(Predicate<ApplicationRuntime> p) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stop(Predicate<ApplicationRuntime> p) {
        appRegistry.find(p).ifPresentOrElse(ApplicationRuntime::stop, () -> {
            throw new ApplicationException("");
        });
    }
}
