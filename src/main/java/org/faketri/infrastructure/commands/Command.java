package org.faketri.infrastructure.commands;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

public enum Command {
    HELP("Show all commands", List.of()),
    PS("Show applications", List.of("-a", "--all", "--filter", "name=", "status=", "id=", "pid=")),
    RUN("Execute application", List.of("name")),
    RESTART("Restart application", List.of("name")),
    STOP("Stop application", List.of("name"));

    private final String description;
    private final List<String> allowedArgs;

    Command(String description, List<String> allowedArgs) {
        this.description = description;
        this.allowedArgs = List.copyOf(allowedArgs);
    }

    @JsonCreator
    public static Command fromString(String key) {
        return Command.valueOf(key.toUpperCase());
    }

    public String description(){
        return description;
    }

    public List<String> allowedArgs(){
        return allowedArgs;
    }

    @Override
    public String toString() {
        return name() +
                " — '" + description + '\'' +
                ", allowedArgs=" + allowedArgs;
    }
}
