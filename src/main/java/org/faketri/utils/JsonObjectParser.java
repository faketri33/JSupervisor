package org.faketri.utils;

import org.faketri.exceptions.unixserver.BadRequestException;
import org.faketri.exceptions.unixserver.JsonProcessingException;
import org.faketri.unixsocket.dto.Frame;
import org.faketri.unixsocket.dto.request.Request;
import org.faketri.unixsocket.dto.response.Response;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;


public class JsonObjectParser {
    private final JsonMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public Request decode(Frame frame) throws BadRequestException {
        return mapper.readValue(frame.payload().array(), Request.class);
    }

    public byte[] encode(Response response) throws JsonProcessingException {
        return mapper.writeValueAsBytes(response);
    }
}
