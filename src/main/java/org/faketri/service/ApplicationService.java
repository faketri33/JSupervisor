package org.faketri.service;


import org.faketri.domain.Application;
import org.faketri.domain.dto.ApplicationView;
import org.faketri.domain.exceptions.application.ApplicationException;
import org.faketri.domain.exceptions.application.ApplicationNotFindException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


// TODO: Refactor the service to act as the executor of the operation
//  (owning the business logic) instead of merely delegating calls to the repository.
public interface ApplicationService {

    Collection<ApplicationView> getAll();

    Collection<ApplicationView> getByProfile(String profile);

    ApplicationView get(UUID id) throws ApplicationNotFindException;

    ApplicationView get(String name) throws ApplicationNotFindException;

    List<ApplicationView> getActive();

    ApplicationView get(long pid) throws ApplicationNotFindException;

    void start(UUID id);

    void start(String name);

    void restart(long pid) throws ApplicationException;

    void restart(UUID id);

    void restart(String name);

    void stop(long pid) throws ApplicationException;

    void stop(UUID id);

    void stop(String name);

    Collection<String> startAll(String profile);

    void save(Application app);
}
