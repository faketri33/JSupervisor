package org.faketri.net.io.dto.request;

import org.faketri.infrastructure.process.filters.Filters;

public record ProcessedRequest(String path, Args args, Filters filters) {
}
