package org.faketri.domain.exceptions.process;

public class ProcessAlreadyRunningException extends ProcessException {
    public ProcessAlreadyRunningException(String message) {
        super(message);
    }
}
