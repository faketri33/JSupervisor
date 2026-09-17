package org.faketri.exceptions;

public class ProcessAlreadyRunningException extends ProcessException {
    public ProcessAlreadyRunningException(String message) {
        super(message);
    }
}
