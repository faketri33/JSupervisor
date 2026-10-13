package org.faketri.service;

import org.faketri.infrastructure.process.ApplicationRuntime;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public interface ApplicationRegistry {

    void add(ApplicationRuntime rt);

    Optional<ApplicationRuntime> remove(UUID id);

    Collection<ApplicationRuntime> findAll(Predicate<ApplicationRuntime> p);

    Optional<ApplicationRuntime> find(Predicate<ApplicationRuntime> p);
}
