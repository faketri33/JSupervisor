package org.faketri.unixsocket;

import org.faketri.unixsocket.dto.Request;

@FunctionalInterface
public interface RequestHandler {
    void handle(Request request);
}
