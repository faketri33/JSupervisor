package org.faketri.net.socket.dto.response;

public record OkResponse(String status) implements Response {
    public OkResponse() {
        this("ok");
    }
}
