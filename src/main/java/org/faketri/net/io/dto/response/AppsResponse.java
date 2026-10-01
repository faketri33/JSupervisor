package org.faketri.net.io.dto.response;

import java.util.List;

public record AppsResponse(String status, List<AppInfo> apps) implements Response {
    public AppsResponse(List<AppInfo> apps) {
        this("ok", apps);
    }
}
