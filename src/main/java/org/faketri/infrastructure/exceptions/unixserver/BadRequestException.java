package org.faketri.infrastructure.exceptions.unixserver;

import org.faketri.net.socket.EStatusCode;

public class BadRequestException extends ErrorRequest {
    public BadRequestException(String message) {
        super(message, EStatusCode.BAD_REQUEST);
    }
}
