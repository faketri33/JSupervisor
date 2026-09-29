package org.faketri.service;

import org.faketri.domain.Application;
import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.faketri.infrastructure.exceptions.application.ApplicationNotFindException;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface ApplicationService {

    Collection<Application> getAll();
    Collection<Application> getByProfile(String profile);

    Application getById(UUID id) throws ApplicationNotFindException ;
    Application getByName(String name) throws ApplicationNotFindException ;
    Application getByPid(long pid) throws ApplicationNotFindException;

    void startByPid(long pid) throws ApplicationException;
    void startById(UUID id);
    void startByName(String name);

    void restartByPid(long pid) throws ApplicationException;
    void restartById(UUID id);
    void restartByName(String name);

    void stopByPid(long pid) throws ApplicationException;
    void stopById(UUID id);
    void stopByName(String name);

    void startAllByProfile(String profile) throws IOException;
    void save(Application app);
}
