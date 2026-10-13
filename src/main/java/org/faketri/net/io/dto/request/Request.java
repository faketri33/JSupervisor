package org.faketri.net.io.dto.request;


import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

public record Request(String command, Args args) {

    @JsonCreator
    public Request {
        args = args == null ? new Args(List.of()) : args;
    }

    public Request(String command, List<String> args) {
        this(command, new Args(args));
    }

    @Override
    public String toString() {
        return "Command " + command + "\n" +
                "Args - " + args;
    }
}
