package org.faketri.service;

import org.faketri.domain.Application;
import org.faketri.domain.dto.ApplicationView;
import org.faketri.domain.dto.mapper.ApplicationMapper;
import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.domain.exceptions.application.ApplicationNotFindException;
import org.faketri.domain.repository.ApplicationRepository;
import org.faketri.infrastructure.process.ApplicationProcessContainer;
import org.faketri.infrastructure.process.journal.InMemoryJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final ApplicationRepository applicationRepository;
    private final Set<ApplicationProcessContainer> activeApp;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
        this.activeApp = ConcurrentHashMap.newKeySet();
    }

    @Override
    public Collection<ApplicationView> getAll() {
        return applicationRepository
                .getAll()
                .stream()
                .map(ApplicationMapper::toDto)
                .toList();
    }

    @Override
    public Collection<ApplicationView> getByProfile(String profile) {
        return applicationRepository
                .getByProfile(profile)
                .stream()
                .map(ApplicationMapper::toDto)
                .toList();
    }

    @Override
    public ApplicationView get(UUID id) throws ApplicationNotFindException {
        return ApplicationMapper.toDto(applicationRepository.get(id));
    }

    @Override
    public ApplicationView get(String name) throws ApplicationNotFindException {
        return ApplicationMapper.toDto(applicationRepository.get(name));
    }

    @Override
    public List<ApplicationView> getActive() {
        return activeApp.stream().map(ApplicationMapper::toDto).toList();
    }

    @Override
    public ApplicationView get(long pid) throws ApplicationNotFindException {
        for (var c : activeApp) {
            Long p = c.pid();
            if (p != null && p == pid) return ApplicationMapper.toDto(c);
        }
        throw new ApplicationNotFindException("pid " + pid);
    }

    @Override
    public void start(UUID id) {
        start(applicationRepository.get(id));
    }

    @Override
    public void start(String name) {
        log.debug("App request to start with name {}", name);
        start(applicationRepository.get(name));
    }

    private void start(Application app) {
        var cont = new ApplicationProcessContainer(app, new InMemoryJournal());
        cont.start();
        log.debug("Application start with name {}", app.getName());
        activeApp.add(cont);
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
        var apps = applicationRepository.getByProfile(profile);
        Collection<String> notes = new ArrayList<>();
        for (var app : apps) {
            try {
                log.debug("Start app {}", app.getName());
                start(app);
                notes.add("Application successfully start - " + app.getName());
            } catch (ApplicationException ex) {
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
