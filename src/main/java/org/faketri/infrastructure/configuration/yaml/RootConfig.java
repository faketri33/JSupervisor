package org.faketri.infrastructure.configuration.yaml;

import org.faketri.domain.Application;
import org.faketri.infrastructure.configuration.yaml.mapper.ConfigMapper;

import java.util.Collection;
import java.util.Map;

public final class RootConfig {
    private Map<String, ConfigEntry> app;

    public Map<String, ConfigEntry> getApp() {
        return app;
    }

    public void setApp(Map<String, ConfigEntry> app) {
        this.app = app;
    }

    public Collection<Application> applications() {
        return app.entrySet()
                .stream()
                .map(entry -> ConfigMapper.toDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}
