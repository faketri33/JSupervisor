package org.faketri.net.io.dto.response;

import java.util.Collection;
import java.util.List;

public record AppInfo(String name, String id, long pid, List<String> command, Collection<String> journal) {
}
