package org.faketri.net.socket.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "command")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RunRequest.class, name = "run"),
        @JsonSubTypes.Type(value = StopRequest.class, name = "stop"),
        @JsonSubTypes.Type(value = AllRequest.class, name = "all")
})
public sealed interface Request permits RunRequest, StopRequest, AllRequest {
}

