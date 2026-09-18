package org.faketri.domain.repository;

import org.faketri.domain.Application;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface ApplicationRepository {

    Collection<Application> getAll();
    Collection<Application> getByProfile(String profile);

    Application getById(UUID id);
    Application getByName(String name);
    Application getByPid(long pid);

    void startAllByProfile(String profile) throws IOException;
    void save(Application app);
}
