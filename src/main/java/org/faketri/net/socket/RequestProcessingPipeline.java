package org.faketri.net.socket;

import org.faketri.infrastructure.commands.Command;
import org.faketri.net.handlers.RequestProcessing;
import org.faketri.net.io.dto.request.Args;
import org.faketri.net.io.dto.request.DispatchableRequest;
import org.faketri.net.io.dto.request.InternalRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public final class RequestProcessingPipeline implements RequestProcessing {

    private static Command parseCommand(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Command must not be empty");
        for (Command c : Command.values()) {
            if (c.name().equalsIgnoreCase(raw)) return c;
        }
        throw new IllegalArgumentException("Unknown command '" + raw + "'. Available: "
                + Arrays.stream(Command.values()).map(c -> c.name().toLowerCase(Locale.ROOT)).toList());
    }

    private static void checkArgs(Command cmd, Args args) {
        String name = cmd.name().toLowerCase(Locale.ROOT);

        for (String flag : args.flagNames()) {
            if (!cmd.flags().contains(flag)) {
                throw new IllegalArgumentException("Unknown option '" + flag + "' for '" + name + "'");
            }
        }

        int n = args.positionalCount();
        switch (cmd.target()) {
            case NONE, MANY -> {
                if (n > 0) throw new IllegalArgumentException("'" + name + "' takes no positional arguments");
            }
            case ONE -> {
                if (n != 1)
                    throw new IllegalArgumentException("'" + name + "' expects exactly one application, got " + n);
            }
        }
    }

    private static void checkTarget(Command cmd, List<String> conditions) {
        String name = cmd.name();
        switch (cmd.target()) {
            case NONE -> {
                if (!conditions.isEmpty()) throw new IllegalArgumentException("'" + name + "' takes no arguments");
            }
            case ONE -> {
                if (conditions.isEmpty()) throw new IllegalArgumentException("'" + name + "' needs a target");
            }
            case MANY -> {
            }
        }
    }

    @Override
    public DispatchableRequest process(InternalRequest request) {
        Command cmd = parseCommand(request.command());
        Args args = new Args(request.args(), cmd.options());
        checkArgs(cmd, args);

        List<String> conditions = new ArrayList<>(args.positionals());
        conditions.addAll(args.values("filter"));
        checkTarget(cmd, conditions);

        return new DispatchableRequest(cmd, args, conditions);
    }
}
