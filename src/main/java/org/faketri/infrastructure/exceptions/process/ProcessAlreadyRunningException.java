package org.faketri.infrastructure.exceptions.process;

public class ProcessAlreadyRunningException extends ProcessException {
    public ProcessAlreadyRunningException(String message) {
        super(message);
    }
}
