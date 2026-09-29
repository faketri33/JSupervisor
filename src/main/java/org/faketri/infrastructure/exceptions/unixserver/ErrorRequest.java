package org.faketri.infrastructure.exceptions.unixserver;

public class ErrorRequest extends RuntimeException {
    public ErrorRequest(String message) {
        super(message);
    }
}
