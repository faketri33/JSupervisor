package org.faketri.infrastructure.repository;

import org.faketri.domain.Application;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.utils.function.CheckedPredicate;
import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.faketri.infrastructure.exceptions.application.ApplicationNotFindException;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationInMemoryRepository implements ApplicationRepository {

    private final Set<Application> applications;

    public ApplicationInMemoryRepository() {
        this.applications = new HashSet<>();
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
    public Application getById(UUID id) throws ApplicationNotFindException {
        return applications.stream()
                .filter(a -> a.getAppId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApplicationNotFindException("Cannot find application with id - " + id));
    }

    public Application getByName(String name) throws ApplicationNotFindException {
        return applications.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ApplicationNotFindException("Cannot find application with name - " + name));
    }

    public Application getByPid(long pid) throws ApplicationException {
        return applications.stream()
                // skip not started process from list, because he doesn't have pid
                .filter(CheckedPredicate.unchecked(a -> a.getPid() == pid))
                .findFirst()
                .orElseThrow(() -> new ApplicationNotFindException("Cannot find application with process pid " + pid));
    }

    public void startAllByProfile(String profile){
        var apps = applications.stream().filter(a -> a.getConfiguration().getProfile().equalsIgnoreCase(profile)).collect(Collectors.toSet());
        for (var app : apps) app.start();
    }

    public void save(Application app) {
        applications.add(app);
    }
}
