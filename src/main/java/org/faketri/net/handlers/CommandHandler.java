package org.faketri.net.handlers;

import org.faketri.infrastructure.exceptions.unixserver.ErrorRequest;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.Response;

@FunctionalInterface
public interface CommandHandler<R extends Request, O extends Response> {
    O handle(R req) throws ErrorRequest;
}
