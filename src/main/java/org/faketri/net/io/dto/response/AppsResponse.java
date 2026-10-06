package org.faketri.net.io.dto.response;

import java.util.Collection;

public record AppsResponse(String status, Collection<AppInfo> apps) implements Response {
    public AppsResponse(Collection<AppInfo> apps) {
        this("ok", apps);
    }
}
