package org.faketri.mapper;

import org.faketri.api.dto.AppConfig;
import org.faketri.api.dto.Application;
import org.faketri.api.dto.parser.ConfigEntry;

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
