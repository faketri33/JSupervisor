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
    public Application getById(UUID id) throws ApplicationNotFindException {
        return applicationRepository.getById(id);
    }

    @Override
    public Application getByName(String name) throws ApplicationNotFindException {
        return applicationRepository.getByName(name);
    }

    @Override
    public Application getByPid(long pid) throws ApplicationNotFindException {
        return applicationRepository.getByPid(pid);
    }

    @Override
    public void startById(UUID id) {
        getById(id).start();
    }

    @Override
    public void startByName(String name) {
        getByName(name).start();
    }

    @Override
    public void restartByPid(long pid) throws ApplicationException {
        throw new UnsupportedOperationException("Restart is not support.");
    }

    @Override
    public void restartById(UUID id) {
        throw new UnsupportedOperationException("Restart is not support.");
    }

    @Override
    public void restartByName(String name) {
        throw new UnsupportedOperationException("Restart is not support.");
    }

    @Override
    public void stopByPid(long pid) throws ApplicationException {
        throw new UnsupportedOperationException("Stop is not support.");
    }

    @Override
    public void stopById(UUID id) {
        throw new UnsupportedOperationException("Stop is not support.");
    }

    @Override
    public void stopByName(String name) {
        throw new UnsupportedOperationException("Stop is not support.");
    }

    @Override
    public void startAllByProfile(String profile) {
        var apps = getByProfile(profile);
        for (var app : apps) app.start();
    }

    @Override
    public void save(Application app) {
        applicationRepository.save(app);
    }
}
