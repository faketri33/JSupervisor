package org.faketri.net.socket;

import org.faketri.infrastructure.commands.Command;
import org.faketri.infrastructure.process.filters.AppRef;
import org.faketri.infrastructure.process.filters.Filters;
import org.faketri.infrastructure.process.filters.RuntimeFilters;
import org.faketri.net.handlers.RequestProcessing;
import org.faketri.net.io.dto.request.ProcessedRequest;
import org.faketri.net.io.dto.request.Request;

public class UnixRequestProcessing implements RequestProcessing {

    @Override
    public ProcessedRequest process(Request request) {
        if (request.command() == null) throw new IllegalArgumentException("Command cannot be a null");
        Command cmd = request.command();

        Filters fl = Filters.all();
        if (!cmd.allowedArgs().isEmpty()) {
            fl = request.args().positionalCount() > 0 ?
                    request.args().positional(0, AppRef::parse)
                    : RuntimeFilters.parse(request.args().values("filter"));
        }
        return new ProcessedRequest(request.command(), request.args(), fl);
    }
}
