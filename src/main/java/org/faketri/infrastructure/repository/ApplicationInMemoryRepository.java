package org.faketri.infrastructure.repository;

import org.faketri.domain.Application;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.faketri.infrastructure.exceptions.application.ApplicationNotFindException;
import org.faketri.infrastructure.process.reader.ConsoleOutputProcessReader;
import org.faketri.utils.function.CheckedPredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationInMemoryRepository implements ApplicationRepository {

    private final Set<Application> applications;

    public ApplicationInMemoryRepository() {
        this.applications = ConcurrentHashMap.newKeySet();
    }

    public Collection<Application> getAll() {
        return new ArrayList<>(applications);
    }

    public Collection<Application> getByProfile(String profile) {
        return applications.stream()
                .filter(a -> a.getConfiguration().getProfile().equalsIgnoreCase(profile))
                .toList();
    }

    @Override
    public Application get(UUID id) throws ApplicationNotFindException {
        return applications.stream()
                .filter(a -> a.getAppId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApplicationNotFindException("Cannot find application with id - " + id));
    }

    public Application get(String name) throws ApplicationNotFindException {
        return applications.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ApplicationNotFindException("Cannot find application with name - " + name));
    }


    public void save(Application app) {
        applications.add(app);
    }
}
