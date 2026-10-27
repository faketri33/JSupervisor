package org.faketri.net.io.dto.request;

import org.faketri.infrastructure.commands.Command;

import java.util.List;

public record DispatchableRequest(Command command, Args args, List<String> conditions) {
}
