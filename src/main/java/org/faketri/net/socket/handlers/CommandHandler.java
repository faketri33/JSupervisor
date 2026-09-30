package org.faketri.net.socket.handlers;

import org.faketri.infrastructure.exceptions.unixserver.ErrorRequest;
import org.faketri.net.socket.dto.request.Request;
import org.faketri.net.socket.dto.response.Response;

@FunctionalInterface
public interface CommandHandler<R extends Request, O extends Response> {
    O handle(R req) throws ErrorRequest;
}
