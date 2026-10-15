package org.faketri.service;

import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.infrastructure.process.ApplicationRuntime;
import org.faketri.infrastructure.process.filters.Filters;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AppRegistryImpl implements ApplicationRegistry {
    private final Map<UUID, ApplicationRuntime> runtimes = new ConcurrentHashMap<>();

    @Override
    public synchronized void add(ApplicationRuntime rt) {
        Objects.requireNonNull(rt);
        boolean taken = runtimes.values().stream()
                .anyMatch(r -> r.id().equals(rt.id()) || r.name().equalsIgnoreCase(rt.name()));
        if (taken) throw new ApplicationException("Already exists: " + rt.name());
        runtimes.put(rt.id(), rt);
    }

    @Override
    public Optional<ApplicationRuntime> remove(UUID id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(runtimes.remove(id));
    }

    @Override
    public Collection<ApplicationRuntime> findAll(Filters p) {
        List<ApplicationRuntime> result = new ArrayList<>();
        for (var app : runtimes.values())
            if (p.test(app)) result.add(app);
        result.sort(Comparator.comparing(ApplicationRuntime::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    @Override
    public Optional<ApplicationRuntime> find(Filters p) {
        for (var app : runtimes.values())
            if (p.test(app)) return Optional.of(app);
        return Optional.empty();
    }
}
