package org.faketri.service;

import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.filter.Query;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.notification.NotificationSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ApplicationLifecycleImpl implements ApplicationLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ApplicationLifecycleImpl.class);
    private final ApplicationRegistry appRegistry;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);


    public ApplicationLifecycleImpl(ApplicationRegistry appRegistry) {
        this.appRegistry = appRegistry;
    }

    @Override
    public void start(Query<ApplicationRuntime> p) {
        appRegistry.find(p).ifPresentOrElse(ApplicationRuntime::start, () -> {
            throw new ApplicationException("Cannot find application for stop");
        });
        NotificationSystem.notify("Application start " + p.describe());
    }

    @Override
    public void restart(Query<ApplicationRuntime> p) {
        stop(p);
        start(p);
    }

    @Override
    public void stop(Query<ApplicationRuntime> p) {
        appRegistry.find(p).ifPresentOrElse(ApplicationRuntime::stop, () -> {
            throw new ApplicationException("Cannot find application for stop");
        });
        NotificationSystem.notify("Application stopped, " + p.describe());
    }
}
