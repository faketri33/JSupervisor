package org.faketri.mapper;

import org.faketri.dto.AppConfig;
import org.faketri.dto.Application;
import org.faketri.dto.parser.ConfigEntry;

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
