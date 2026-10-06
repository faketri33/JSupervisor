package org.faketri.net.handlers;

import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.request.Request;
import org.faketri.net.io.dto.response.Response;

/**
 * Handles a specific type of {@link Request} and produces a corresponding {@link Response}.
 *
 * <p>Implementations are intended to be stateless with respect to a single invocation
 * and are suitable for use as lambda expressions or method references.
 *
 * @param <R> type of the request handled by this handler
 * @param <O> type of the response produced by this handler
 */
@FunctionalInterface
public interface CommandHandler<R extends Request, O extends Response> {

    /**
     * Handles the specified request and returns the resulting response.
     *
     * @param req request to handle
     * @return response produced for the given request
     * @throws ErrorRequest if the request cannot be processed
     */
    O handle(R req) throws ErrorRequest;
}
