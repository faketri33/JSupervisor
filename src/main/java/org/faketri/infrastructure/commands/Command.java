package org.faketri.infrastructure.commands;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Set;

public enum Command {
    HELP("Show all commands", Target.NONE, Set.of(), Set.of()),
    PS("Show applications", Target.MANY, Set.of("a", "all"), Set.of("filter")),
    RUN("Execute application", Target.ONE, Set.of(), Set.of()),
    RESTART("Restart application", Target.ONE, Set.of(), Set.of()),
    STOP("Stop application", Target.ONE, Set.of(), Set.of());

    public enum Target {
        NONE, ONE, MANY,
    }

    private final String description;
    private final Target target;
    private final Set<String> flags;
    private final Set<String> options;

    Command(String description, Target target, Set<String> flags, Set<String> options) {
        this.description = description;
        this.target = target;
        this.flags = Set.copyOf(flags);
        this.options = Set.copyOf(options);
    }

    @JsonCreator
    public static Command fromString(String key) {
        return Command.valueOf(key.toUpperCase());
    }

    public String description() {
        return description;
    }

    public Set<String> flags() {
        return flags;
    }

    public Set<String> options() {
        return options;
    }

    public Target target() {
        return target;
    }

    @Override
    public String toString() {
        return name() +
                " — '" + description() + '\'' +
                ", flags=" + flags();
    }
}
