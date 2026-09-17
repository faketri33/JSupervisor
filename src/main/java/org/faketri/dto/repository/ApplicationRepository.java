package org.faketri.dto.repository;

import org.faketri.dto.Application;

import java.util.Collection;
import java.util.UUID;

public interface ApplicationRepository {

    Collection<Application> getAll();
    Collection<Application> getByProfile(String profile);

    Application getById(UUID id);
    Application getByName(String name);
    Application getByPid(long pid);

    void startAllByProfile(String profile) ;
    void save(Application app);
}
