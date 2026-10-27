package org.faketri.infrastructure.controllers;

import org.faketri.infrastructure.commands.Command;
import org.faketri.net.RequestDispatcherCommand;
import org.faketri.net.io.dto.request.RequestContext;
import org.faketri.net.io.dto.response.ResponseEntry;

import java.util.Arrays;
import java.util.List;

public class HelpController implements Controller {

    public ResponseEntry<List<String>> help(RequestContext<Void> request) {
        return ResponseEntry.ok(Arrays.stream(Command.values()).map(Command::toString).toList());
    }

    @Override
    public void register(RequestDispatcherCommand dispatcherCommand) {
        dispatcherCommand.register(Command.HELP, this::help);
    }

}
