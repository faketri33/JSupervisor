package org.faketri.net.handlers;

import org.faketri.net.io.dto.request.Request;

@FunctionalInterface
public interface VoidHandler<R extends Request> {
    void handle(R req);
}
