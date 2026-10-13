package org.faketri.service;

import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.process.ApplicationRuntime;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class AppRegistryImpl implements ApplicationRegistry {
    private final Map<UUID, ApplicationRuntime> runtimes = new ConcurrentHashMap<>();

    @Override
    public synchronized void add(ApplicationRuntime rt) {
        boolean taken = runtimes.values().stream()
                .anyMatch(r -> r.id().equals(rt.id()) || r.name().equals(rt.name()));
        if (taken) throw new ApplicationException("Already exists: " + rt.name());
        runtimes.put(rt.id(), rt);
    }

    @Override
    public Optional<ApplicationRuntime> remove(UUID id) {
        return Optional.ofNullable(runtimes.remove(id));
    }

    @Override
    public Collection<ApplicationRuntime> findAll(Predicate<ApplicationRuntime> p) {
        return runtimes
                .values()
                .stream()
                .filter(p)
                .toList();
    }

    @Override
    public Optional<ApplicationRuntime> find(Predicate<ApplicationRuntime> p) {
        return runtimes.values().stream().filter(p).findFirst();
    }
}
