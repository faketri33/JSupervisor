package org.faketri.service;

import org.faketri.infrastructure.filter.Query;
import org.faketri.infrastructure.process.ApplicationRuntime;

public interface ApplicationLifecycle {

    void start(Query<ApplicationRuntime> p);

    void restart(Query<ApplicationRuntime> p);

    void stop(Query<ApplicationRuntime> p);
}
