package org.faketri.net.io.dto.request;

import org.faketri.infrastructure.commands.Command;
import org.faketri.infrastructure.process.filters.Filters;

public record ProcessedRequest(Command command, Args args, Filters filters) {
}
