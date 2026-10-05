package org.faketri.net.handlers;

import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.OkResponse;
import org.faketri.net.io.dto.response.Response;

public final class ResponseEntry {
    private ResponseEntry() {}

    public static <R extends Request> CommandHandler<R, Response> ok(VoidHandler<R> h) {
        return req -> {
            h.handle(req);
            return new OkResponse();
        };
    }

}
