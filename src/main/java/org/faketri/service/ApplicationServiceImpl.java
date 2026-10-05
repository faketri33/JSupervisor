package org.faketri.service;

import org.faketri.domain.Application;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.faketri.infrastructure.exceptions.application.ApplicationNotFindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private final ApplicationRepository applicationRepository;
    private final Set<Application> activeApp;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
        this.activeApp = ConcurrentHashMap.newKeySet();
    }

    @Override
    public Collection<Application> getAll() {
        return applicationRepository.getAll();
    }

    @Override
    public Collection<Application> getByProfile(String profile) {
        return applicationRepository.getByProfile(profile);
    }

    @Override
    public Application get(UUID id) throws ApplicationNotFindException {
        return applicationRepository.get(id);
    }

    @Override
    public Application get(String name) throws ApplicationNotFindException {
        return applicationRepository.get(name);
    }

    @Override
    public Application get(long pid) throws ApplicationNotFindException {
        return applicationRepository.get(pid);
    }

    @Override
    public void start(UUID id) {
        get(id).start();
    }

    @Override
    public void start(String name) {
        get(name).start();
    }

    @Override
    public void restart(long pid) throws ApplicationException {
        throw new UnsupportedOperationException("Restart is not support.");
    }

    @Override
    public void restart(UUID id) {
        throw new UnsupportedOperationException("Restart is not support.");
    }

    @Override
    public void restart(String name) {
        throw new UnsupportedOperationException("Restart is not support.");
    }

    @Override
    public void stop(long pid) throws ApplicationException {
        throw new UnsupportedOperationException("Stop is not support.");
    }

    @Override
    public void stop(UUID id) {
        throw new UnsupportedOperationException("Stop is not support.");
    }

    @Override
    public void stop(String name) {
        throw new UnsupportedOperationException("Stop is not support.");
    }

    @Override
    public Collection<String> startAll(String profile) {
        var apps = getByProfile(profile);
        Collection<String> notes = new ArrayList<>();
        for (var app : apps) {
            try {
                log.debug("Start app {}", app.getName());
                app.start();
                activeApp.add(app);
                notes.add("Application successfully start - " + app.getName());
            } catch (ApplicationException ex){
                log.error(ex.getMessage());
                notes.add("Application error - " + app.getName());
            }
        }
        return notes;
    }

    @Override
    public void save(Application app) {
        applicationRepository.save(app);
    }
}
