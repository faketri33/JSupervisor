package org.faketri.service;

import org.faketri.infrastructure.filter.Query;
import org.faketri.infrastructure.process.ApplicationRuntime;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRegistry {

    void add(ApplicationRuntime rt);

    Optional<ApplicationRuntime> remove(UUID id);

    Collection<ApplicationRuntime> findAll(Query<ApplicationRuntime> p);

    Optional<ApplicationRuntime> find(Query<ApplicationRuntime> p);
}
