package org.faketri.net.io.dto.request;


import com.fasterxml.jackson.annotation.JsonCreator;
import org.faketri.infrastructure.commands.Command;

import java.util.List;

public record Request(Command command, Args args) {

    @JsonCreator
    public Request {
        args = args == null ? new Args(List.of()) : args;
    }

    public Request(Command command, List<String> args) {
        this(command, new Args(args));
    }

    @Override
    public String toString() {
        return "Command " + command + "\n" +
                "Args - " + args;
    }
}
