package org.faketri.exceptions.unixserver;

public class BadRequestException extends ErrorRequest {
    public BadRequestException(String message) {
        super(message);
    }
}
