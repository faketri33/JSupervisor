package org.faketri.api.dto;

import java.util.UUID;

public class AppId {
    private final UUID uuid;

    public AppId() {
        this.uuid = UUID.randomUUID();
    }

    public UUID getId(){
        return uuid;
    }
}
