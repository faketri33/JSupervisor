package org.faketri.unixsocket.handlers;

import org.faketri.unixsocket.dto.request.Request;
import org.faketri.unixsocket.dto.response.Response;

@FunctionalInterface
public interface CommandHandler<R extends Request, O extends Response>{
    O handle(R req) throws Exception;
}
