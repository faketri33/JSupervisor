package org.faketri.domain.repository;

import org.faketri.domain.Application;

import java.util.Collection;
import java.util.UUID;

public interface ApplicationRepository extends Repository<Application, UUID> {

    Collection<Application> getAll();

    Collection<Application> getByProfile(String profile);

    Application get(UUID id);

    Application get(String name);

    void save(Application app);
}
