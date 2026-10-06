package org.faketri.service;

import org.faketri.domain.Application;
import org.faketri.infrastructure.exceptions.application.ApplicationException;
import org.faketri.infrastructure.exceptions.application.ApplicationNotFindException;
import org.faketri.infrastructure.process.ApplicationProcessContainer;
import org.faketri.net.io.dto.response.AppInfo;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


// TODO: Refactor the service to act as the executor of the operation
//  (owning the business logic) instead of merely delegating calls to the repository.
public interface ApplicationService {

    Collection<AppInfo> getAll();

    Collection<AppInfo> getByProfile(String profile);

    AppInfo get(UUID id) throws ApplicationNotFindException;

    AppInfo get(String name) throws ApplicationNotFindException;

    List<AppInfo> getActive();

    AppInfo get(long pid) throws ApplicationNotFindException;

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
