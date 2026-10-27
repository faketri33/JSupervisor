package org.faketri.net.handlers;

import org.faketri.net.exceptions.request.ErrorRequest;
import org.faketri.net.io.dto.request.RequestContext;
import org.faketri.net.io.dto.response.ResponseEntry;

/**
 * Handles a specific type of {@link RequestContext} and produces a corresponding {@link ResponseEntry}.
 *
 * <p>Implementations are intended to be stateless with respect to a single invocation
 * and are suitable for use as lambda expressions or method references.
 *
 */
@FunctionalInterface
public interface CommandHandler<T> {

    /**
     * Handles the specified request and returns the resulting response.
     *
     * @param req request to handle
     * @return response produced for the given request
     * @throws ErrorRequest if the request cannot be processed
     */
    ResponseEntry<?> handle(RequestContext<T> req) throws ErrorRequest;
}
