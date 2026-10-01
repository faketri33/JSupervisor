package org.faketri.net.io.dto.response;


public record ErrorResponse(String status, String code, String message) implements Response {
    public ErrorResponse(String code, String message) {
        this("error", code, message);
    }
}
