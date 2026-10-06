package org.faketri.net.io.mapper;

import org.faketri.domain.Application;
import org.faketri.infrastructure.process.ApplicationProcessContainer;
import org.faketri.net.io.dto.response.AppInfo;

import java.util.List;

public final class AppMapper {
    private AppMapper() {
    }

    public static AppInfo map(Application app) {
        return new AppInfo(app.getName(), app.getAppId().toString(), -1, app.getConfiguration().getCommands(), List.of());
    }

    public static AppInfo map(ApplicationProcessContainer app) {
        var a = app.getApplication();
        return new AppInfo(a.getName(), a.getAppId().toString(), app.getProcess().pid(), a.getConfiguration().getCommands(), app.journal());
    }
}
