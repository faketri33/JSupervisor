package org.faketri.infrastructure.exceptions.unixserver;

public class BadRequestException extends ErrorRequest {
    public BadRequestException(String message) {
        super(message);
    }
}
