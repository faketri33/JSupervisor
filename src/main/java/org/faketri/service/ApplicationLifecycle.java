package org.faketri.service;

import org.faketri.infrastructure.process.filters.Filters;

public interface ApplicationLifecycle {

    void start(Filters p);

    void restart(Filters p);

    void stop(Filters p);
}
