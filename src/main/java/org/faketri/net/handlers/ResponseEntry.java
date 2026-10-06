package org.faketri.net.handlers;

import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.OkResponse;
import org.faketri.net.io.dto.response.Response;

/**
 * Provides adapters for creating {@link CommandHandler} instances
 * with predefined responses.
 *
 * <p>Allows a {@link VoidHandler} to be used as a {@link CommandHandler}
 * when request processing does not produce a result but the protocol
 * still requires a response.</p>
 */
public final class ResponseEntry {

    private ResponseEntry() {
    }

    /**
     * Creates a command handler that executes the given handler and
     * returns an {@link OkResponse} after successful execution.
     *
     * @param h   handler responsible for processing the request
     * @param <R> type of request handled by the handler
     * @return command handler that returns an {@link OkResponse}
     */
    public static <R extends Request> CommandHandler<R, Response> ok(VoidHandler<R> h) {
        return req -> {
            h.handle(req);
            return new OkResponse();
        };
    }
}
