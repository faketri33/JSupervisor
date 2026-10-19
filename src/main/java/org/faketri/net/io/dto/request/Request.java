package org.faketri.net.io.dto.request;

import java.util.List;

public record Request(String command, List<String> args) {

    @Override
    public String toString() {
        return "Command " + command + "\n" +
                "Args - " + args;
    }
}
