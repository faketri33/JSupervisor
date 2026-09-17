package org.faketri.dto.parser;

import java.util.Map;

public class RootConfig {
    private Map<String, ConfigEntry> app;

    public Map<String, ConfigEntry> getApp() {
        return app;
    }

    public void setApp(Map<String, ConfigEntry> app) {
        this.app = app;
    }
}
