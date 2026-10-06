package org.faketri.domain.exceptions.application;

public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }
}
