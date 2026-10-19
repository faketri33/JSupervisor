package org.faketri.net.socket;

import org.faketri.infrastructure.commands.Command;
import org.faketri.infrastructure.process.filters.AppRef;
import org.faketri.infrastructure.process.filters.Filters;
import org.faketri.infrastructure.process.filters.RuntimeFilters;
import org.faketri.net.handlers.RequestProcessing;
import org.faketri.net.io.dto.request.Args;
import org.faketri.net.io.dto.request.ProcessedRequest;
import org.faketri.net.io.dto.request.Request;

import java.util.Arrays;
import java.util.Locale;

public class UnixRequestProcessing implements RequestProcessing {

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

    private static Filters resolveTarget(Command cmd, Args args) {
        return switch (cmd.target()) {
            case NONE -> Filters.all();
            case ONE -> args.positional(0, AppRef::parse);
            case MANY -> RuntimeFilters.parse(args.values("filter"));
        };
    }

    @Override
    public ProcessedRequest process(Request request) {
        Command cmd = parseCommand(request.command());
        Args args = new Args(request.args(), cmd.options());
        checkArgs(cmd, args);
        return new ProcessedRequest(cmd, args, resolveTarget(cmd, args));
    }
}
