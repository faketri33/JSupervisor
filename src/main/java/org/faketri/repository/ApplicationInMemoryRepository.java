package org.faketri.repository;

import org.faketri.domain.Application;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.exceptions.application.ApplicationNotFindException;
import org.faketri.utils.CheckedPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ApplicationInMemoryRepository implements ApplicationRepository {

    private static final Logger log = LoggerFactory.getLogger(ApplicationInMemoryRepository.class);

    private final Set<Application> applications;

    public ApplicationInMemoryRepository() {
        this.applications = new HashSet<>();
    }

    public Collection<Application> getAll(){
        return new ArrayList<>(applications);
    }

    public Collection<Application> getByProfile(String profile){
        return applications.stream()
                .filter(a -> a.getConfiguration().getProfile().equalsIgnoreCase(profile))
                .toList();
    }

    @Override
    public Application getById(UUID id) {
        return applications.stream()
                .filter(a -> a.getAppId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApplicationNotFindException("Cannot find application with id - " + id));
    }

    public Application getByName(String name){
        return applications.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ApplicationNotFindException("Cannot find application with name - " + name));
    }

    public Application getByPid(long pid){
        return applications.stream()
            // skip not started process from list
            .filter(CheckedPredicate.unchecked(a -> a.getPid() == pid))
            .findFirst()
            .orElseThrow(() -> new ApplicationNotFindException("Cannot find application with process pid " + pid));
    }

    public void startAllByProfile(String profile) throws IOException {
        Collection<Application> apps = getByProfile(profile);
        for (var app : apps) app.start();
    }

    public void save(Application app){
        applications.add(app);
    }
}
