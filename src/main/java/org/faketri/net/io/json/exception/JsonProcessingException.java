package org.faketri.net.io.json.exception;

import org.faketri.infrastructure.configuration.exception.ParserException;

public class JsonProcessingException extends ParserException {
    public JsonProcessingException(String message) {
        super(message);
    }
}
