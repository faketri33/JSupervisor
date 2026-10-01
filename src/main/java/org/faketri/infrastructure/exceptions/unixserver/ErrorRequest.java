package org.faketri.infrastructure.exceptions.unixserver;

import org.faketri.net.EStatusCode;

public class ErrorRequest extends RuntimeException {

    private final EStatusCode code;

    public ErrorRequest(String message, EStatusCode code) {
        super(message);
        this.code = code;
    }

    public String getCode(){
        return code.name();
    }
}
