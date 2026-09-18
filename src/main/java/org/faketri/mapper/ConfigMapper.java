package org.faketri.mapper;

import org.faketri.domain.AppConfig;
import org.faketri.domain.Application;
import org.faketri.domain.parser.ConfigEntry;

public class ConfigMapper {
    private ConfigMapper(){}

    public static Application toDto(String name, ConfigEntry entry){
        return Application.of(name,
                new AppConfig.Builder()
                        .commands(entry.getCommand())
                        .maxRestartCount(entry.getMaxRestartCount())
                        .profile(entry.getProfile())
                        .setRestartable(entry.isRestart())
                .build());
    }
}
