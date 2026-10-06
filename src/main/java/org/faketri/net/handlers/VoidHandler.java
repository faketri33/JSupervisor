package org.faketri.net.handlers;

import org.faketri.net.io.dto.request.Request;

/**
 * Handles a {@link Request} without producing a response.
 *
 * <p>Used when request processing is performed for its side effects and
 * the caller does not need a result from the handler itself. A response
 * can be created separately by adapting this handler to a
 * {@link CommandHandler}.</p>
 *
 * @param <R> type of request handled by this handler
 */
@FunctionalInterface
public interface VoidHandler<R extends Request> {

    /**
     * Handles the given request.
     *
     * @param req request to process
     */
    void handle(R req);
}
