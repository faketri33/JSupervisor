package org.faketri.net.exceptions.request;

import org.faketri.net.EStatusCode;

public class BadRequestException extends ErrorRequest {
    public BadRequestException(String message) {
        super(message, EStatusCode.BAD_REQUEST);
    }
}
