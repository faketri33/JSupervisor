package org.faketri.service;

import org.faketri.infrastructure.process.ApplicationRuntime;

import java.util.function.Predicate;

public interface ApplicationLifecycle {

    void start(Predicate<ApplicationRuntime> p);

    void restart(Predicate<ApplicationRuntime> p);

    void stop(Predicate<ApplicationRuntime> p);
}
