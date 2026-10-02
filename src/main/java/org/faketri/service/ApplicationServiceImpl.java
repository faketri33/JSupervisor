package org.faketri.service;

import org.faketri.domain.Application;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.faketri.infrastructure.exceptions.application.ApplicationNotFindException;

import java.util.Collection;
import java.util.UUID;

public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
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
    public void startAll(String profile) {
        var apps = getByProfile(profile);
        for (var app : apps) app.start();
    }

    @Override
    public void save(Application app) {
        applicationRepository.save(app);
    }
}
