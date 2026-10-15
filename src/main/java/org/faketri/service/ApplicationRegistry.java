package org.faketri.service;

import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.Filters;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRegistry {

    void add(ApplicationRuntime rt);

    Optional<ApplicationRuntime> remove(UUID id);

    Collection<ApplicationRuntime> findAll(Filters p);

    Optional<ApplicationRuntime> find(Filters p);
}
