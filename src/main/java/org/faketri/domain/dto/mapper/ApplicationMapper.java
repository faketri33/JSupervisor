package org.faketri.domain.dto.mapper;

import org.faketri.domain.Application;
import org.faketri.domain.State;
import org.faketri.domain.dto.ApplicationView;
import org.faketri.infrastructure.process.ApplicationProcessContainer;

import java.util.List;


public final class ApplicationMapper {
    private ApplicationMapper() {
    }

    public static ApplicationView toDto(ApplicationProcessContainer cont) {
        var app = cont.application();
        return new ApplicationView(
                app.getAppId(),
                app.getName(),
                cont.getState(),
                cont.pid(),
                cont.isAlive(),
                app.getConfiguration(),
                cont.log());
    }

    public static ApplicationView toDto(Application app) {
        return new ApplicationView(
                app.getAppId(),
                app.getName(),
                State.PENDING,
                -1L,
                false,
                app.getConfiguration(),
                List.of());
    }
}
