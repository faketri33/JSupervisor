package org.faketri.domain.dto;

import org.faketri.domain.AppConfig;
import org.faketri.domain.Application;
import org.faketri.domain.State;

import java.util.Collection;
import java.util.UUID;

public record AppSnapshot(
        UUID appId,
        String name,
        State state,
        Long pid,
        boolean alive,
        AppConfig configuration,
        Collection<String> journal
) {
    public AppSnapshot(Application app, long pid, boolean alive, Collection<String> journal) {
        this(app.getAppId(), app.getName(), app.getState(), pid, alive, app.getConfiguration(), journal);
    }
}
